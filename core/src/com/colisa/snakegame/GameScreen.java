package com.colisa.snakegame;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class GameScreen extends ScreenAdapter {
    private static final float MOVE_TIME = 1f;
    private static final int SNAKE_MOVEMENT = 32;
    private int snakeX = 0;
    private int snakeY = 0;
    private float timer = MOVE_TIME;

    // Directions
    private static final int RIGHT = 0;
    private static final int LEFT = 1;
    private static final int UP = 2;
    private static final int DOWN = 3;
    private int snakeDirection = RIGHT;

    private SpriteBatch batch;
    private Texture snakeHead;
    private Texture apple;
    private boolean appleAvailable = false;
    private int appleX, appleY;
    private Texture snakeBody;
    private Array<BodyPart> bodyParts;
    private int snakeXBeforeUpdate = 0, snakeYBeforeUpdate = 0;

    private ShapeRenderer shapeRenderer;
    private static final int GRID_CELL = 32;

    private boolean directionSet;
    private boolean hasHit = false;


    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        bodyParts = new Array<BodyPart>();

        snakeHead = new Texture("snakehead.png");
        apple = new Texture("apple.png");
        snakeBody = new Texture("snakebody.png");
    }

    @Override
    public void render(float delta) {
        queryInput();
        updateSnake(delta);
        checkAppleCollision();
        checkAndPlaceApple();

        clearScreen();
        drawGrid();
        draw();

    }

    private void updateSnake(float delta) {
        if (!hasHit){
            timer -= delta;
            if (timer < 0) {
                timer = MOVE_TIME;
                moveSnake();
                checkForOutOfBounds();
                updateBodyPartsPosition();
                checkSnakeCollision();
                directionSet = false;
            }
        }

    }

    private void clearScreen() {
        Gdx.gl.glClearColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void draw() {
        batch.begin();

        batch.draw(snakeHead, snakeX, snakeY);
        for (BodyPart bodyPart : bodyParts) bodyPart.draw(batch);
        if (appleAvailable) batch.draw(apple, appleX, appleY);

        batch.end();
    }

    private void drawGrid() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int x = 0; x <= Gdx.graphics.getWidth(); x += GRID_CELL) {
            for (int y = 0; y <= Gdx.graphics.getHeight(); y += GRID_CELL) {
                shapeRenderer.rect(x, y, GRID_CELL, GRID_CELL);
            }
        }
        shapeRenderer.end();
    }

    private void checkAppleCollision() {
        if (appleAvailable && snakeX == appleX && snakeY == appleY) {
            BodyPart bodyPart = new BodyPart(snakeBody);
            bodyPart.updateBodyPart(snakeX, snakeY);
            bodyParts.insert(0, bodyPart);
            appleAvailable = false;
        }
    }

    private void checkForOutOfBounds() {
        if (snakeX >= Gdx.graphics.getWidth()) snakeX = 0;
        if (snakeX < 0) snakeX = Gdx.graphics.getWidth() - SNAKE_MOVEMENT;
        if (snakeY >= Gdx.graphics.getHeight()) snakeY = 0;
        if (snakeY < 0) snakeY = Gdx.graphics.getHeight() - SNAKE_MOVEMENT;
    }

    private void moveSnake() {
        snakeXBeforeUpdate = snakeX;
        snakeYBeforeUpdate = snakeY;

        switch (snakeDirection) {
            case RIGHT:
                snakeX += SNAKE_MOVEMENT;
                break;
            case LEFT:
                snakeX -= SNAKE_MOVEMENT;
                break;
            case UP:
                snakeY += SNAKE_MOVEMENT;
                break;
            case DOWN:
                snakeY -= SNAKE_MOVEMENT;
                break;
        }
    }

    private void checkSnakeCollision() {
        for (BodyPart body : bodyParts)
            if (body.x == snakeX && body.y == snakeY) hasHit = true;
    }

    private void updateBodyPartsPosition() {
        if (bodyParts.size > 0) {
            BodyPart bodyPart = bodyParts.removeIndex(0);
            bodyPart.updateBodyPart(snakeXBeforeUpdate, snakeYBeforeUpdate);
            bodyParts.add(bodyPart);
        }
    }

    private void queryInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))    updateDirection(LEFT);
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))   updateDirection(RIGHT);
        if (Gdx.input.isKeyPressed(Input.Keys.UP))      updateDirection(UP);
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))    updateDirection(DOWN);
    }

    private void updateIfNotOppositeDirection(int newSnakeDirection, int oppositeDirection) {
        if (snakeDirection != oppositeDirection) snakeDirection = newSnakeDirection;
    }

    private void updateDirection(int newDirection) {
        if (!directionSet && snakeDirection != newDirection) {
            directionSet = true;
            switch (newDirection) {
                case LEFT:
                    updateIfNotOppositeDirection(newDirection, RIGHT);
                    break;
                case RIGHT:
                    updateIfNotOppositeDirection(newDirection, LEFT);
                    break;
                case UP:
                    updateIfNotOppositeDirection(newDirection, DOWN);
                    break;
                case DOWN:
                    updateIfNotOppositeDirection(newDirection, UP);
                    break;
            }
        }

    }

    private void checkAndPlaceApple() {
        if (!appleAvailable) {
            do {
                appleX = MathUtils.random(Gdx.graphics.getWidth() / SNAKE_MOVEMENT - 1) * SNAKE_MOVEMENT;
                appleY = MathUtils.random(Gdx.graphics.getHeight() / SNAKE_MOVEMENT - 1) * SNAKE_MOVEMENT;
                appleAvailable = true;
            } while (appleX == snakeX && appleY == snakeY);
        }
    }

    @Override
    public void dispose() {
        snakeBody.dispose();
        apple.dispose();
        snakeHead.dispose();
        shapeRenderer.dispose();
        batch.dispose();
    }

    private class BodyPart {
        private int x, y;
        private Texture texture;

        public BodyPart(Texture texture) {
            this.texture = texture;
        }

        public void updateBodyPart(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void draw(Batch batch) {
            if (!(x == snakeX && y == snakeY)) batch.draw(texture, x, y);
        }
    }
}
