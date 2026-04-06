# 🏙️ Wordtropolis

> *Restore the city. Master the words.*

Wordtropolis is an interactive, gamified spelling application set in a pixel-style city environment where players complete word-based challenges to restore order after a villain disrupts the city. The game combines storytelling with educational tasks, transforming spelling practice into an engaging journey.

---

## Table of Contents

- [About the Project](#about-the-project)
- [Target Users](#target-users)
- [Core Features](#core-features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
  - [Requirements](#requirements)
  - [Cloning the Repository](#cloning-the-repository)
  - [Running in NetBeans](#running-in-netbeans)
  - [Building an Executable JAR](#building-an-executable-jar)
  - [Running the Executable JAR](#running-the-executable-jar)
- [Gameplay Overview](#gameplay-overview)
- [Authors](#authors)

---

## About the Project

The name **Wordtropolis** combines *word* (spelling) with *metropolis* (city), creating a meaningful conceptual model that helps users immediately understand the application's purpose. Players navigate through different districts of a pixel-art city, completing spelling activities to restore each area. The journey culminates in a final boss battle where mastery of spelling is put to the ultimate test. The player is a hero fighting the villain which is the final boss.

This design is effective because it integrates educational content with interactive gameplay and adaptive feedback, encouraging active learning and sustained engagement rather than passive memorization.

---

## Target Users

| User | Role |
|------|------|
| **Students (kindergarten to Grade 2)** | Primary users — learn and practice spelling through gameplay |
| **Teachers** | Secondary users — input and customize spelling word lists for their students |

---

## Core Features

- **City-based game structure** — players complete activities to restore different areas of the city
- **Multiple spelling activity types:**
  - Word Search
  - Missing Letters
  - Mixed/Scrambled Words
  - Alphabetical Ordering
- **Adaptive learning system** — tracks mistakes and reinforces weak areas through targeted challenges (including the boss fight)
- **Teacher customization** — educators can input custom spelling word lists
- **Narrative-driven progression** — story-based motivation to complete challenges

---

## Tech Stack

- **Language:** Java, Java Swing
- **IDE:** NetBeans
- **Build Tool: Maven** 

---

## Getting Started

### Requirements

Before you begin, ensure you have the following installed:

-  [Java 17 SDK](https://jdk.java.net/17/) (or newer)
-  [Apache Maven](https://maven.apache.org/) (version 3.6+ recommended)
-  [NetBeans IDE](https://netbeans.apache.org/) (only if you want to build project in NetBeans)

To verify your Java installation:
```bash
java -version
javac -version
```

To verify your Maven installation:
```bash
mvn -version
```
---

### Cloning the Repository

```bash
git clone [fill in the link]
cd wordtropolis
```

---

### Running in NetBeans

1. Open **NetBeans IDE**
2. Go to **File → Open Project**
3. Navigate to the cloned `wordtropolis` folder and select it
4. Wait for NetBeans to index the project then open `wordtropolis.java` file
5. Click the **Run** button (▶) or press `F6`

---

### Building an Executable JAR

You can package Wordtropolis into a standalone `.jar` file that anyone with Java installed can run — no IDE required.

#### Option A: Using NetBeans (Recommended)

1. Open the project in NetBeans
2. Go to **Run → Clean and Build Project** (or press `Shift + F11`)
3. NetBeans will generate the JAR file at:
```
wordtropolis/target/Wordtropolis.jar
```

#### Option B: Using a Terminal

This project uses Maven to build a **JAR with all dependencies included**.

##### 1. Open a Terminal

* **Windows:** `Command Prompt` or `PowerShell`
* **Linux/macOS:** `Terminal`

##### 2. Navigate to the Project Directory

```bash
cd /path/to/wordtropolis
```

Replace `/path/to/wordtropolis` with the location of the project root (where `pom.xml` is located).

##### 3. Compile and Package the Project

Run the following Maven command:

```bash
mvn clean package
```

* `clean` removes any previous build artifacts.
* `package` compiles the project and creates a JAR with dependencies.


### Running the Executable JAR

Once you have the `.jar` file built:

#### From the Terminal / Command Prompt

```bash
java -jar target/Wordtropolis.jar
```

> ⚠️ **Note:** If the game uses a GUI, make sure you are running this on a system with a display (not a headless server).

#### On Windows — Double-Click to Run

If `.jar` files are associated with Java on your machine, you can simply double-click `Wordtropolis.jar` to launch the game.

If double-clicking doesn't work, right-click the `.jar` → **Open with** → **Java Platform SE Binary**.


---

## Gameplay Overview

1. **Start in the city hub** —  begin with easy challenges 
2. **Complete spelling challenges** like saving a cat, catching a burglar, repairing a bridge and putting out a fire
3. **Earn progress** and unlock new areas as the city is restored
4. **Face the boss** — a final challenge that targets the words you struggled with most
5. **Victory!** — the city is saved and your spelling improves along the way


---

## Authors

Introducing Our Team!:

1. Anna Nguyen
2. Sarah Solaiman
3. Sarah VanDuzer
4. Vaishnavi Gudimella
5. Yedam Lee
6. Yukta Asit Mehta

Please note: This project was created for course COMPSCI 4474 - Human-Computer Interaction at Western University.

---

