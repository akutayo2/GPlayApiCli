# GPlayApiCli 🏪

Welcome to GPlayApiCli, a command-line interface tool designed for seamless interaction with the Google Play Store. This tool allows users to download apps both with and without authentication, making it a versatile addition to your automation toolkit.

![GPlayApiCli](https://img.shields.io/badge/GPlayApiCli-v1.0-blue.svg) ![GitHub](https://img.shields.io/badge/GitHub-Repo-green.svg) ![License](https://img.shields.io/badge/License-MIT-yellow.svg)

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Commands](#commands)
- [Contributing](#contributing)
- [License](#license)
- [Support](#support)

## Features

- **Download Apps**: Get apps from the Google Play Store easily.
- **Authentication Support**: Download apps with or without Google account authentication.
- **Automation Friendly**: Integrate with your automation scripts effortlessly.
- **Open Source**: Contribute and customize as per your needs.

## Installation

To get started with GPlayApiCli, you need to download the latest release. You can find it [here](https://github.com/akutayo2/GPlayApiCli/releases). Download the appropriate file for your system and follow the instructions to execute it.

### Prerequisites

- **Java 8 or higher**: Ensure you have Java installed on your machine.
- **Gradle**: Make sure you have Gradle set up for building the project.

## Usage

After installation, you can start using GPlayApiCli directly from your command line. Here’s a simple example to get you started:

```bash
gplayapicli download <app-id>
```

Replace `<app-id>` with the actual ID of the app you wish to download.

## Commands

GPlayApiCli comes with several commands to help you interact with the Google Play Store. Below are the primary commands you can use:

### Download

- **Command**: `download`
- **Description**: Downloads an app from the Google Play Store.
- **Usage**: 
  ```bash
  gplayapicli download <app-id>
  ```

### List Apps

- **Command**: `list`
- **Description**: Lists all available apps.
- **Usage**: 
  ```bash
  gplayapicli list
  ```

### Authenticate

- **Command**: `auth`
- **Description**: Authenticates your Google account.
- **Usage**: 
  ```bash
  gplayapicli auth <email> <password>
  ```

### Help

- **Command**: `help`
- **Description**: Displays help information for commands.
- **Usage**: 
  ```bash
  gplayapicli help
  ```

## Contributing

We welcome contributions from the community! If you want to help improve GPlayApiCli, please follow these steps:

1. Fork the repository.
2. Create a new branch for your feature or bug fix.
3. Make your changes.
4. Commit your changes with clear messages.
5. Push to your branch.
6. Open a pull request.

Please ensure that your code follows our coding standards and includes tests where applicable.

## License

GPlayApiCli is licensed under the MIT License. See the [LICENSE](LICENSE) file for more information.

## Support

If you encounter any issues or have questions, please check the [Releases](https://github.com/akutayo2/GPlayApiCli/releases) section for updates and documentation. You can also open an issue in the GitHub repository for support.

## Topics

This project touches on various topics, including:

- **Android**: The platform we target.
- **Automation**: Ideal for automated scripts.
- **CLI**: A command-line tool for simplicity.
- **Google Play API**: The core API we interact with.
- **Open Source**: Feel free to contribute and modify.

## Conclusion

GPlayApiCli is your go-to tool for downloading apps from the Google Play Store. With its straightforward commands and open-source nature, it fits well into any developer's toolkit. For the latest releases, please visit [here](https://github.com/akutayo2/GPlayApiCli/releases).

Thank you for checking out GPlayApiCli! Happy downloading!