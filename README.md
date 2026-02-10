<div align="center">
  <a href="https://github.com/oshriagronov/brick-breaker">
    <img src="logo.png" alt="Logo" width="200" height="200">
  </a>
<h3 align="center">Brick Breaker</h3>
  <p align="center">
    Traditional brick breaker game where the player needs to break all the bricks with the ball and the paddle surface.
  </p>
</div>

## About

This project is a classic Brick Breaker game developed in Java. The game was built from the ground up, utilizing the javax.swing and java.awt libraries to handle the graphical user interface and event-driven programming.<br/>
This implementation demonstrates a solid understanding of object-oriented principles and game loop mechanics, providing a fun and engaging user experience while showcasing the capabilities of Java for desktop applications.

### Key features

- Parameters are customizable: ball speed, life points, Screen resolution.
- Life points system.
- Score system.

## Future plans
 - Possible bonuses that appear randomly when bricks are destroyed
 - Adding a break that requires two hits to destroy. 
 - And more!

## Technologies Used

- Java SE17.
- Utilities library - Scanner.
- Swing library.
- AWT library.
- Javax library.
- Io library.

## Media

> Start menu.
<img src="./img/menu-screen.jpg" alt="Game menu screen" width="600" height="600">

> Gameplay screen.
<img src="./img/game-screen.jpg" alt="Gameplay screen" width="600" height="600">

> "Game over" screen.
<img src="./img/game-over-screen.jpg" alt="Game over screen" width="600" height="600">

## Getting Started

To get a local copy up and running follow these simple steps.

### Prerequisites

- Linux, MacOS or Windows
- IDE with java

### Installation

---

1. **Clone and enter the Magnetron repository:**

   ```bash
   git clone https://github.com/oshriagronov/brick-breaker && cd brick-breaker
   ```

2. **Open the brick-breaker folder in your IDE**

3. **Run Gamemanager.java file!**

## Executable Package (No IDE Needed)

You can build a runnable package (JAR + assets + launch scripts) and upload the zip to GitHub Releases so players can download it and run the game directly.

### Build the package

**macOS / Linux**

```bash
./scripts/package.sh
```

**Windows (PowerShell)**

```powershell
.\scripts\package.ps1
```

This creates `dist/BrickBreaker.zip`.

### How players run it

1. Download and unzip `BrickBreaker.zip`.
2. Run `run.bat` (Windows) or `run.sh` (macOS/Linux), or double-click `BrickBreaker.jar`.

> Note: Java 17+ must be installed for the JAR to run.


## Acknowledgements

I would like to thank CampusIL and the team behind the "Object oriented programming" course!

> Link to the course home page [here](https://campus.gov.il/course/huji_acd_rfp4_huji_oop/)
