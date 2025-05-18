package xyz.regulad

import com.aurora.gplayapi.DeviceManager
import com.aurora.gplayapi.GooglePlayApi
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.helpers.AppDetailsHelper
import com.aurora.gplayapi.helpers.AuthHelper
import com.aurora.gplayapi.helpers.PurchaseHelper
import com.aurora.gplayapi.data.models.PlayFile
import com.aurora.store.data.model.Auth
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.findOrSetObject
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.types.file
import com.google.gson.Gson
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.URI
import java.net.URL
import java.util.Locale
import java.util.Properties
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/*
 * State shared between parent command & subcommand, primarily used to pass initialized API objects to subcommands.
 */
data class SharedState(
    val authData: AuthData,
    val googlePlayApi: GooglePlayApi,
    val gson: Gson,
    val okHttpClient: OkHttpClient
)

/**
 * The URL of the public Aurora Dispenser. Can be used to get an API key.
 */
const val PUBLIC_AURORA_DISPENSER = "https://auroraoss.com/api/auth"

/**
 * User-Agent for the public Aurora Store that must be used on the Aurora Dispenser GET request.
 */
const val PUBLIC_AURORA_STORE_UA = "com.aurora.store-4.3.6-20240306"
    
class GPlayApiCli : CliktCommand() {
    override fun help(context: Context): String = "A CLI for the Google Play API."
    
    val auroraDispenserUriString: HttpUrl? by argument(
        name = "dispenser", 
        help = "A URL of an Aurora Dispenser (https://github.com/whyorean/AuroraDispenser) that can be accessed to yield an API key."
    )
        .convert {
            URI(it).toURL().toHttpUrlOrNull() ?: fail("Not a valid URL: $it")
        }
        .optional()
    
    val emailProvided: String? by argument(
        name = "email", 
        help = "An email address that can be used to authenticate with the Google Play API. Use of a fixed token is preferred to the use of a dispenser."
    )
        .optional()
    
    val apiKeyProvided: String? by argument(
        name = "key", 
        help = "An API key (AUTH, not AAS) that can be used to authenticate with the Google Play API. Use of a fixed token is preferred to the use of a dispenser."
    )
        .optional()
    
    val locale: Locale by argument(
        name = "locale", 
        help = "The locale to use for the Google Play API. Defaults to the host's locale."
    )
        .convert { Locale.forLanguageTag(it) }
        .default(Locale.getDefault())
    
    val deviceProperties: Properties by argument(
        name = "device", 
        help = "A file path to a properties file containing device properties. " +
                "Device properties determine the device used when authenticating with the Google Play API. " +
                "Different devices may download different apps/receive different responses. " +
                "If the device begins with `included:`, the device will be loaded from the provided devices. " +
                "See https://gitlab.com/AuroraOSS/gplayapi/-/tree/master/lib/src/main/res/raw?ref_type=heads for included devices and examples of what a device property file should look like. " +
                "Defaults to `included:px_9a.properties`, a default gplayapi device."
    )
        .convert {
            if (it.startsWith("included:")) {
                DeviceManager.loadProperties(it.removePrefix("included:"))!!
            } else {
                Properties().apply {
                    File(it).inputStream().use { fd ->
                        load(fd)
                    }
                }
            }
        }
        .default(DeviceManager.loadProperties("px_9a.properties")!!)
    
    @OptIn(ExperimentalAtomicApi::class)
    val sharedStateRef by findOrSetObject<AtomicReference<SharedState?>> { AtomicReference(null) }
    
    @OptIn(ExperimentalAtomicApi::class)
    override fun run() {
        val gson = Gson()
        val client = OkHttpClient()
        
        val email: String
        val apiKey: String
        
        if (emailProvided != null && apiKeyProvided != null) {
            email = emailProvided!!
            apiKey = apiKeyProvided!!
        } else {
            val urlToUse: HttpUrl
            
            if (auroraDispenserUriString == null) {
                System.err.println("Neither a dispenser URL nor an email and API key were provided. Using the public Aurora Dispenser.")
                urlToUse = URI(PUBLIC_AURORA_DISPENSER).toURL().toHttpUrlOrNull()!!
            } else {
                urlToUse = auroraDispenserUriString!!
            }
            
            client.newCall(
                Request.Builder()
                    .url(urlToUse)
                    .header("User-Agent", PUBLIC_AURORA_STORE_UA)
                    .build()
            )
                .execute()
                .use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("Aurora Dispenser returned a non-200 response: ${response.code}")
                    }
                    val auth = gson.fromJson(response.body!!.string(), Auth::class.java)

                    email = auth.email
                    apiKey = auth.auth
                }
        }

        val authData = AuthHelper.build(
            email = email,
            token = apiKey,
            tokenType = AuthHelper.Token.AUTH,
            isAnonymous = false,
            properties = deviceProperties,
            locale = locale
        )
        
        val googlePlayApi = GooglePlayApi()
        
        sharedStateRef.store(
            SharedState(
                authData, 
                googlePlayApi, 
                gson,
                client
            )
        )
    }
}

class GPlayDownload : CliktCommand(name = "download") {
    override fun help(context: Context): String = "Downloads a free app from Google Play. Currently only supports free apps."
    
    @OptIn(ExperimentalAtomicApi::class)
    val sharedStateRef by requireObject<AtomicReference<SharedState?>>()

    val packageId: String by argument(
        name = "id", 
        help = "The package ID of the app to download. Example: `com.instagram.android`"
    )
    val outputPath: String by argument(
        name = "path", 
        help = "The folder to save the downloaded app into. Defaults to the current directory. Will be created if it does not exist."
    )
        .default(".")
    
    val versionCode: Long? by argument(
        name = "version",
        help = "The version code of the app to download. If unset, downloads the newest version."
    )
        .convert { it.toLong() }
        .optional()

    @OptIn(ExperimentalAtomicApi::class)
    override fun run() {
        val sharedState = sharedStateRef.load()!!
        val outputFile = File(outputPath)

        if (outputFile.exists() && !outputFile.isDirectory) {
            throw Exception("$outputPath already exists and is not a directory")
        }

        if (!outputFile.exists()) {
            System.out.println("Creating directory $outputPath to save downloaded app into.")
            outputFile.mkdirs()
        }

        // Download logic using PurchaseHelper
        val purchaseHelper = PurchaseHelper(sharedState.authData)
        // Fetch latest version if versionCode is null
        val actualVersionCode = versionCode ?: run {
            // Use AppDetailsHelper to fetch latest version code
            val appDetailsHelper = AppDetailsHelper(sharedState.authData)
            val details = appDetailsHelper.getAppByPackageName(packageId)
            details.versionCode
        }
        val playFiles: List<PlayFile> = purchaseHelper.purchase(
            packageName = packageId,
            versionCode = actualVersionCode,
            offerType = 1 // Free app
        )
        for (file in playFiles) {
            val fileUrl = file.url
            val fileName = file.name.ifBlank { "base.apk" }
            val destFile = File(outputFile, fileName)
            System.out.println("Downloading ${fileName}...")
            val request = Request.Builder().url(fileUrl).build()
            sharedState.okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("Failed to download ${fileName}: ${response.code}")
                destFile.outputStream().use { out ->
                    response.body?.byteStream()?.copyTo(out)
                }
            }
            System.out.println("Saved to ${destFile.absolutePath}")
        }
        System.out.println("Download complete.")
    }
}

fun main(args: Array<String>) = GPlayApiCli()
    .subcommands(GPlayDownload())
    .main(args)
