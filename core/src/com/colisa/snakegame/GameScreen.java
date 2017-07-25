package com.colisa.snakegame;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen extends ScreenAdapter {
    private static final String TAG = GameScreen.class.getName();
    private static final float MOVE_TIME = 1f;
    private static final int SNAKE_MOVEMENT = 32;
    private int snakeX = 0;
    private int snakeY = 0;
    private float timer = MOVE_TIME;
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
    private STATE state = STATE.PLAYING;

    private enum STATE {PLAYING, GAME_OVER}

    private BitmapFont bitmapFont;
    private GlyphLayout layout;
    private static final String GAME_OVER_TEXT = "Game Over . . Tap SPACE to restart";
    private int score;
    private static final int POINT_PER_APPLE = 20;
    private static final float WORLD_WIDTH = 640.0f;
    private static final float WORLD_HEIGHT = 480.0f;
    private Viewport viewport;
    private Camera camera;


    @Override
    public void show() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        camera.update();
        viewport = new FillViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        bitmapFont = new BitmapFont();
        layout = new GlyphLayout();
        bodyParts = new Array<BodyPart>();

        snakeHead = new Texture("snakehead.png");
        apple = new Texture("apple.png");
        snakeBody = new Texture("snakebody.png");
        score = 0;
    }

    @Override
    public void render(float delta) {
        switch (state) {
            case PLAYING: {
                queryInput();
                updateSnake(delta);
                checkAppleCollision();
                checkAndPlaceApple();
            }
            break;
            case GAME_OVER: {
                checkRestart();
            }
            break;
        }


        clearScreen();
        //drawGrid();
        draw();

    }

    private void updateSnake(float delta) {
        if (state == STATE.PLAYING) {
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
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(snakeHead, snakeX, snakeY);
        for (BodyPart bodyPart : bodyParts) bodyPart.draw(batch);
        if (appleAvailable) batch.draw(apple, appleX, appleY);

        if (state == STATE.GAME_OVER) {
            layout.setText(bitmapFont, GAME_OVER_TEXT);
            bitmapFont.draw(
                    batch,
                    GAME_OVER_TEXT,
                    viewport.getWorldWidth() / 2,
                    viewport.getWorldHeight() / 2,
                    0,
                    Align.center,
                    false);
        }

        drawScore();
        batch.end();
    }

    private void drawGrid() {
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int x = 0; x <= viewport.getWorldWidth(); x += GRID_CELL) {
            for (int y = 0; y <= viewport.getWorldHeight(); y += GRID_CELL) {
                shapeRenderer.rect(x, y, GRID_CELL, GRID_CELL);
            }
        }
        shapeRenderer.end();
    }

    private void checkAppleCollision() {
        if (appleAvailable && snakeX == appleX && snakeY == appleY) {
            addScore();
            BodyPart bodyPart = new BodyPart(snakeBody);
            bodyPart.updateBodyPart(snakeX, snakeY);
            bodyParts.insert(0, bodyPart);
            appleAvailable = false;
        }
    }

    private void checkForOutOfBounds() {
        if (snakeX >= viewport.getWorldWidth()) snakeX = 0;
        if (snakeX < 0) snakeX = (int) viewport.getWorldWidth() - SNAKE_MOVEMENT;
        if (snakeY >= viewport.getWorldHeight()) snakeY = 0;
        if (snakeY < 0) snakeY = (int) viewport.getWorldHeight() - SNAKE_MOVEMENT;
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
            if (body.x == snakeX && body.y == snakeY) state = STATE.GAME_OVER;
    }

    private void updateBodyPartsPosition() {
        if (bodyParts.size > 0) {
            BodyPart bodyPart = bodyParts.removeIndex(0);
            bodyPart.updateBodyPart(snakeXBeforeUpdate, snakeYBeforeUpdate);
            bodyParts.add(bodyPart);
        }
    }

    private void queryInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) updateDirection(LEFT);
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) updateDirection(RIGHT);
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) updateDirection(UP);
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) updateDirection(DOWN);
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
                appleX = MathUtils.random((int) viewport.getWorldWidth() / SNAKE_MOVEMENT - 1) * SNAKE_MOVEMENT;
                appleY = MathUtils.random((int) viewport.getWorldHeight() / SNAKE_MOVEMENT - 1) * SNAKE_MOVEMENT;
                appleAvailable = true;
            } while (appleX == snakeX && appleY == snakeY);
        }
    }

    @Override
    public void dispose() {
        bitmapFont.dispose();
        snakeBody.dispose();
        apple.dispose();
        snakeHead.dispose();
        shapeRenderer.dispose();
        batch.dispose();
    }

    private void addScore() {
        score += POINT_PER_APPLE;
    }

    private void drawScore() {
        if (state == STATE.PLAYING) {
            String scoringString = Integer.toString(score);
            layout.setText(bitmapFont, scoringString);
            bitmapFont.draw(
                    batch, scoringString, layout.height * 2, viewport.getWorldHeight() - layout.height * 2
            );
        }
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

    private void checkRestart() {
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) restartGame();
    }

    private void restartGame() {
        state = STATE.PLAYING;
        bodyParts.clear();
        snakeDirection = RIGHT;
        snakeY = 0;
        snakeX = 0;
        directionSet = false;
        timer = MOVE_TIME;
        snakeXBeforeUpdate = 0;
        snakeYBeforeUpdate = 0;
        appleAvailable = false;
        score = 0;
    }

    @Override
    public void resize(int width, int height) {

    }
}
