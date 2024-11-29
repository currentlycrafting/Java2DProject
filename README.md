# README: Circular AI Game Implementation

## Overview
This project is a Java-based game developed using `javax.swing` for creating a simple graphical interface. The game features a player controlled by keyboard input and enemies that exhibit circular AI behavior, attempting to surround and trap the player. The game includes a timer and obstacles on the map to increase the challenge.

## Features
- **Player Movement**: The player can move in all directions using arrow keys or WASD keys.
- **Enemy AI**: Enemies move in a circular pattern around the player, dynamically adjusting their position to close in and trap the player.
- **Game Timer**: Tracks and displays the elapsed game time.
- **Enemy Spawning**: Enemies are spawned at intervals, with conditions to ensure they are not placed too close to the player or within obstacles.
- **Collision Detection**: Prevents the player and enemies from moving through obstacles and checks for collisions between entities.
- **Game Over State**: The game ends when an enemy collides with the player.

## Installation
To run this game, you need Java Development Kit (JDK) installed on your system. Follow these steps:

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/circular-ai-game.git
   cd circular-ai-game
   ```

2. **Compile the Java files**:
   ```bash
   javac GamePanel.java
   ```

3. **Run the game**:
   ```bash
   java GamePanel
   ```

## Code Explanation

### GamePanel Class
- **Inheritance and Interfaces**: The class extends `JPanel` and implements `Runnable` to manage the game loop in a separate thread.
- **Constants and Variables**:
  - `originalTileSize`, `scale`, `tileSize`: Control the size of the tiles on the map.
  - `screenWidth`, `screenHeight`: Define the dimensions of the game window.
  - `playerX`, `playerY`, `playerSpeed`: Track the player's position and speed.
  - `map`: A 2D array representing the game grid, where `1` indicates an obstacle and `0` represents open space.
  - `enemies`: An `ArrayList` to store enemy objects.
  - `gameOver`: A boolean flag indicating whether the game has ended.

### Circular AI Movement
The enemies use a circular AI pattern to approach and encircle the player. Here’s how it works:
- **Angle Adjustment**: Each enemy updates its movement angle with a slight randomness (`enemy.angle += Math.random() * Math.PI / 8 - Math.PI / 16`) to make the movement less predictable.
- **Target Calculation**: The target position for an enemy is computed using the player’s current position and the radius of the circular movement:
  ```java
  int targetX = (int) (playerX + circleRadius * Math.cos(enemy.angle));
  int targetY = (int) (playerY + circleRadius * Math.sin(enemy.angle));
  ```
- **Direction and Movement**:
  - The enemy calculates the distance to its target position and normalizes the movement vector.
  - A `slowDownFactor` is applied to reduce the speed and make the approach smoother.
- **Collision Handling**: The enemy checks for potential collisions with other enemies and obstacles to adjust its path.

### Game Loop and Updates
- The `run()` method is executed in a separate thread to continuously update the game state at a fixed frame rate (`FPS`).
- The `update()` method handles:
  - Player movement based on keyboard input.
  - Collision checks.
  - Enemy behavior updates.
  - Spawning of new enemies at a set interval.
- The `paintComponent()` method is responsible for rendering the game elements, including the player, enemies, and obstacles.

## How the Circular AI Traps the Player
The primary goal of the circular AI is to limit the player's movement options by surrounding them. The enemy's circular approach ensures it stays at a calculated distance around the player, dynamically adjusting its path to cut off escape routes. The enemies' consistent movement in a circular pattern reduces the player's space to maneuver, gradually encircling them until a collision occurs.

## Key Methods Explained
- **`update()`**: Updates the player and enemy states, handles collisions, and spawns new enemies.
- **`spawnNewEnemy()`**: Generates a new enemy at a valid location, ensuring it does not spawn on top of the player or in a restricted area.
- **`isCollidingWithObstacle()`**: Checks if a given position collides with an obstacle.
- **`isCollidingWithEntity()`**: Checks if the player is colliding with an enemy.
- **`paintComponent(Graphics g)`**: Renders the game scene, including the player, enemies, and map.

## Future Enhancements
- **Improved AI**: Implement more advanced enemy behavior, such as coordinated group movement.
- **Game Levels**: Add multiple levels with different map layouts and increasing difficulty.
- **Power-Ups**: Introduce items that the player can collect to gain temporary advantages.
- **Graphics and UI**: Enhance the visuals with better artwork and more complex UI elements.

## Acknowledgements
- **Java Swing**: Used for the graphical user interface and rendering.
- **Mathematics**: Utilized trigonometric functions to implement the circular movement pattern.

## License
This project is licensed under the MIT License. You are free to use, modify, and distribute this code for personal and educational purposes.
