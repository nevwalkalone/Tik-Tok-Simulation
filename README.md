# First Assignment

Read this [section](https://github.com/nevwalkalone/Tik-Tok-Simulation/tree/main), (if you haven't already) before proceeding.

In this assignment, the Video Streaming Framework (Event Delivery System) is implemented.
It will be responsible for uploading and downloading videos and it will be the core of our
Backend System for the Second Assignment too. Java sockets and Multithreading are used for the
network communication.

## Videos Dataset

This [dataset](https://drive.google.com/file/d/1DyrLKpwRLdJXIkGJKM0Hs_AC6A8Z4BZz/view?usp=sharing) must be used as sample videos for the application.

## Environment

- [Java 8](https://www.oracle.com/java/technologies/java8.html) or above
- [Intellij Idea](https://www.jetbrains.com/idea/)
- [lib](https://drive.google.com/file/d/1ghrkUKw2kOqvNDFAjVOWQchRTYW7AGVH/view?usp=sharing) folder which contains the 2 external libraries that must be added to the project.
    - commons-codec-1.14.jar
    - tika-app-1.26.jar

## Summary

### <ins> Event Delivery System </ins>

The Event Delivery System model is a programming framework that allows to send and receive
data that meet specific criteria. The advantage of this system is that it allows 
to send and receive data in real time, by using two key functions, "push" and "pull".
These two functions are independent of each other. In every call of the "push" function,
there must be proper care so that the intermediate node in the system, called **Broker** can
simultaneously receive data from different publishers (in our case content creators), to be able 
to properly transfer the results to the final subscribers (who are also called **Consumers**).
Also, parallelism is necessary because the system must offer the ability to simultaneously
transfer data from  content creators to the intermediate nodes, and from the intermediate nodes to the
subscribers, as all subscribed users should receive the same content at the same time.



## Usage
1. **Clone** repository
   ```console
   git clone https://github.com/nevwalkalone/POSIX-Projects.git
    ```
2. **Change** directory to Tik-Tok-Simulation
   ```console
   cd Tik-Tok-Simulation
    ```
3. **Change** branch to 1st-Assignment
    ```console
     git checkout 1st-Assignment
    ```
4. **Open** Tik-Tok-Simulation folder as a project in Intellij
5. After downloading the Videos [dataset](https://drive.google.com/file/d/1DyrLKpwRLdJXIkGJKM0Hs_AC6A8Z4BZz/view?usp=sharing) place the videos_dataset folder
6. After downloading the [lib](https://drive.google.com/file/d/1ghrkUKw2kOqvNDFAjVOWQchRTYW7AGVH/view?usp=sharing) folder add the 2 external libraries to the project via File ->
Project Structure -> Modules and the + button
7. Via Run -> Edit Configurations -> Modify options for both the Broker and AppNode classes,
 enable the Allow multiple instances option
8. Run 3 Broker instances 
9. Run 2 AppNode instances. For each AppNode instance a prompt will appear asking for the username.
**If the username is either Producer A or Producer B, a channel initialization containing the videos 
in videos_dataset\Producer A (or Producer B) will take place. If this is not the case, no
channel initialization takes place**.
10. A menu list will then appear asking for the user to select an option.
    - **1**: Request a topic
    - **2**: Subscribe to a topic (hashtag or channel)
    - **3**: Refresh
    - **4**: Publish a new video
    - **5**: Delete a video
    - **6**: Exit
    
In this assignment no Frontend was required, so this is only a simulation.

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
