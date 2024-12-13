package com.proyecto.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Intersector;

import java.util.Random;

public class FlappyBirdGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture background;
    private Texture[] birdFrames;
    private Texture tubeTop;
    private Texture tubeBottom;

    private float birdX;
    private float birdY;
    private float birdVelocity = 2f;
    private final float gravity = 3f;
    private final int birdWidth = 50;
    private final int birdHeight = 50;

    private int gameState = 0;
    private int score = 0;
    private int scoringTube = 0;

    private float[] tubesX = new float[4];
    private float[] tubesOffset = new float[4];
    private final int tubeWidth = 100;
    private final int gap = 400;
    private final float tubeVelocity = 4f;

    private int frameCount = 0;
    private Rectangle birdRectangle;
    private Rectangle[] topTubeRectangles = new Rectangle[4];
    private Rectangle[] bottomTubeRectangles = new Rectangle[4];

    private float tubeSpawnTime = 0;
    private final float tubeSpawnInterval = 1.2f; // Genera tuberías cada 3 segundos

    @Override
    public void create() {
        batch = new SpriteBatch();
        background = new Texture("background.png");
        birdFrames = new Texture[]{
            new Texture("pajaro1.png"),
            new Texture("pajaro2.png")
        };
        tubeTop = new Texture("tuberia_arriba.png");
        tubeBottom = new Texture("tuberia_abajo.png");

        birdX = Gdx.graphics.getWidth() / 4f - birdWidth / 2f;
        birdY = Gdx.graphics.getHeight() / 2f;
        birdRectangle = new Rectangle();

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        Random random = new Random();

        // Ajustamos la posición de las tuberías
        for (int i = 0; i < tubesX.length; i++) {
            tubesX[i] = screenWidth / 2f + i * (screenWidth / 2f); // Ubicación horizontal
        }

        topTubeRectangles = new Rectangle[4];
        bottomTubeRectangles = new Rectangle[4];
        for (int i = 0; i < tubesX.length; i++) {
            topTubeRectangles[i] = new Rectangle();
            bottomTubeRectangles[i] = new Rectangle();
        }
    }

    @Override
    public void render() {
        frameCount++;
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Generar nuevas tuberías cada 3 segundos
        tubeSpawnTime += Gdx.graphics.getDeltaTime();
        if (tubeSpawnTime >= tubeSpawnInterval) {
            tubeSpawnTime = 0;  // Reiniciar el temporizador para generar la próxima tubería

            // Reposicionar las tuberías fuera de la pantalla si han salido
            for (int i = 0; i < tubesX.length; i++) {
                if (tubesX[i] < -tubeWidth) {
                    tubesX[i] = Gdx.graphics.getWidth();  // Colocar la tubería al borde derecho

                    // Generar un hueco aleatorio para las tuberías
                    tubesOffset[i] = (new Random().nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - gap - 100);  // Controla el tamaño del hueco
                }
            }
        }

        // Dibujamos las tuberías
        for (int i = 0; i < tubesX.length; i++) {
            tubesX[i] -= tubeVelocity + 10;  // Mover las tuberías a la izquierda

            // Tubería superior
            batch.draw(tubeTop, tubesX[i], Gdx.graphics.getHeight() / 2f + gap / 2f + tubesOffset[i]);

            // Tubería inferior
            batch.draw(tubeBottom, tubesX[i], Gdx.graphics.getHeight() / 2f - gap / 2f - tubeBottom.getHeight() + tubesOffset[i]);

            // Actualizamos las posiciones de las colisiones
            topTubeRectangles[i].set(tubesX[i], Gdx.graphics.getHeight() / 2f + gap / 2f + tubesOffset[i], tubeTop.getWidth(), tubeTop.getHeight());
            bottomTubeRectangles[i].set(tubesX[i], Gdx.graphics.getHeight() / 2f - gap / 2f - tubeBottom.getHeight() + tubesOffset[i], tubeBottom.getWidth(), tubeBottom.getHeight());
        }

        // Animar el pájaro
        Texture currentBirdFrame = birdFrames[(frameCount / 10) % 2];
        batch.draw(currentBirdFrame, birdX, birdY, birdWidth, birdHeight);
        birdRectangle.set(birdX, birdY, birdWidth, birdHeight);

        // Lógica del juego
        if (gameState == 1) {
            birdVelocity += gravity;
            birdY -= birdVelocity;

            // Colisiones con las tuberías
            for (int i = 0; i < tubesX.length; i++) {
                if (Intersector.overlaps(birdRectangle, topTubeRectangles[i]) || Intersector.overlaps(birdRectangle, bottomTubeRectangles[i])) {
                    gameState = 2; // Fin del juego si colisiona
                }
            }

            if (birdY <= 0) {
                gameState = 2; // Si el pájaro toca el suelo, el juego termina
            }
        } else if (gameState == 0 && Gdx.input.justTouched()) {
            gameState = 1; // Iniciar el juego si se toca la pantalla
        }

        // Salto del pájaro
        if (gameState == 1 && Gdx.input.justTouched()) {
            birdVelocity = -30f;
        }

        batch.end();
    }
}


