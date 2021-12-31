# TikTok Simulation

These 2 projects were completed for the Distributed Systems course of the [Department of Computer Science of the Athens University of Economics and Business](https://www.dept.aueb.gr/el/cs), during the Spring semester of 2020-2021.

As mentioned in the About Section, these university projects compose a Distributed Application which simulates TikTok. This application is a video streaming on demand service which is based on the **Publisher/Subscriber** model. Backend System makes use of Java sockets and Multithreading for the network communication. Frontend System is implemented on Android Studio.

Users can subscribe to other users or topics, as well as become "content creators" themselves, i.e. create and share multimedia content.
Each user maintains a channel, that is, a space where they can add new content (videos) which can then be viewed by various subscribers of this channel through the network. Every user can either record a video and upload it at that moment, or upload a video from their gallery. Whenever a video gets uploaded to the platform, it becomes available to all users who are subscribed to the specific channel.

Development takes place in 2 phases/projects:

1. First phase/project implements the Video Streaming Framework (Event Delivery System) which will be responsible for uploading and downloading videos. This will be the core of our **Backend System**.
2. Second phase/project implements the Android application (**Frontend System**), which will use the Framework of our Backend System that was created in the first phase. Videos must play on the android emulator or a mobile device.

<em>For more info on each project, scroll below.</em>

## Videos Dataset

This [dataset](https://drive.google.com/file/d/1DyrLKpwRLdJXIkGJKM0Hs_AC6A8Z4BZz/view?usp=sharing) was used as sample videos for the application.

## Environment

- [Java 8](https://www.oracle.com/java/technologies/java8.html) or above
- [Android Studio](https://developer.android.com/studio)
- [Intellij Idea](https://www.jetbrains.com/idea/)

## Ηow to Switch Between Projects

Each Project is organized into its own branch. To view on Github or download the code locally, you must switch to the corresponding branch.

- [main](https://github.com/nevwalkalone/Tik-Tok-Simulation) branch contains general info and summary of this repository
- [1st-Assignment](https://github.com/nevwalkalone/Tik-Tok-Simulation/tree/1st-Assignment) branch contains the 1st Project
- [2nd-Assignment](https://github.com/nevwalkalone/Tik-Tok-Simulation/tree/2nd-Assignment) branch contains the 2nd Project

## Collaborators

- [nevwalkalone](https://github.com/nevwalkalone)
- [Petros247](https://github.com/Petros247)
- [frostedpenguin](https://github.com/frostedpenguin)

## Contributions

If you want to contribute, you can always create a pull request or open an issue.

## License

[MIT](LICENSE)
