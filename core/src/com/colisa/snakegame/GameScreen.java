package com.colisa.snakegame;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

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

    @Override
    public void show() {
        batch = new SpriteBatch();
        snakeHead = new Texture("snakehead.png");
        apple = new Texture("apple.png");
    }

    @Override
    public void render(float delta) {
        queryInput();

        timer -= delta;
        if (timer < 0) {
            timer = MOVE_TIME;
            moveSnake();
            checkForOutOfBounds();
        }
        checkAndPlaceApple();
        clearScreen();
        draw();

    }

    private void clearScreen(){
        Gdx.gl.glClearColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void draw(){
        batch.begin();
        if (appleAvailable)
            batch.draw(apple, appleX, appleY);

        batch.draw(
                snakeHead,
                snakeX,
                snakeY
        );

        batch.end();
    }

    private void checkForOutOfBounds() {
        if (snakeX >= Gdx.graphics.getWidth())
            snakeX = 0;

        if (snakeX < 0)
            snakeX = Gdx.graphics.getWidth() - SNAKE_MOVEMENT;

        if (snakeY >= Gdx.graphics.getHeight())
            snakeY = 0;

        if (snakeY < 0)
            snakeY = Gdx.graphics.getHeight() - SNAKE_MOVEMENT;

    }

    private void moveSnake() {
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

    private void queryInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) snakeDirection = LEFT;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) snakeDirection = RIGHT;
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) snakeDirection = UP;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) snakeDirection = DOWN;
    }

    private void checkAndPlaceApple() {
        if (!appleAvailable){
            do {
                appleX = MathUtils.random(Gdx.graphics.getWidth() / SNAKE_MOVEMENT - 1) * SNAKE_MOVEMENT;
                appleY = MathUtils.random(Gdx.graphics.getHeight() / SNAKE_MOVEMENT - 1) * SNAKE_MOVEMENT;
                appleAvailable = true;
            }while (appleX == snakeX && appleY == snakeY);
        }
    }

    @Override
    public void dispose() {
        apple.dispose();
        snakeHead.dispose();
        batch.dispose();
    }
}
