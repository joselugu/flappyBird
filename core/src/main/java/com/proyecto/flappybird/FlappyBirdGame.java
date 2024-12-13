package com.proyecto.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

public class FlappyBirdGame extends ApplicationAdapter {
    // Variables para el dibujo de la interfaz gráfica
    SpriteBatch loteSprites;
    Texture fondo;
    Texture finJuego;

    // Variables para el pájaro
    Texture[] pajaro;
    int estadoAleteo = 0; // Indica si el pájaro está aleteando o no
    float posYPajaro = 0; // Posición vertical del pájaro
    float velocidad = 0; // Velocidad de caída del pájaro
    Circle circuloPajaro; // Círculo para detectar colisiones del pájaro
    int puntuacion = 0; // Puntuación actual
    int tuboPuntuacion = 0; // Controla cuál tubo se está usando para puntuar
    BitmapFont fuente; // Fuente para dibujar la puntuación

    // Estado del juego: 0 - esperando, 1 - jugando, 2 - fin del juego
    int estadoJuego = 0;
    float gravedad = 2; // Gravedad que afecta al pájaro

    // Variables para los tubos
    Texture tuboArriba;
    Texture tuboAbajo;
    float espacioTuberias = 600; // Espacio entre el tubo superior e inferior
    float desplazamientoMaxTubo; // Máximo desplazamiento del tubo en el eje Y
    Random generadorAleatorio; // Generador de números aleatorios para posicionar los tubos
    float velocidadTubo = 5; // Velocidad de movimiento de los tubos
    int cantidadTuberias = 4; // Cantidad de tubos visibles en pantalla
    float[] posXTuberias = new float[cantidadTuberias]; // Posiciones en X de los tubos
    float[] desplazamientoTuberias = new float[cantidadTuberias]; // Desplazamiento de cada tubo en el eje Y
    float distanciaEntreTuberias; // Distancia entre cada par de tubos
    Rectangle[] rectangulosTuboArriba; // Rectángulos para detectar colisiones con el tubo superior
    Rectangle[] rectangulosTuboAbajo; // Rectángulos para detectar colisiones con el tubo inferior

    @Override
    public void create () {
        // Inicialización de objetos gráficos
        loteSprites = new SpriteBatch();
        fondo = new Texture("background.png"); // Imagen de fondo
        finJuego = new Texture("gameover.png"); // Imagen de fin de juego
        circuloPajaro = new Circle(); // Círculo para la detección de colisiones
        fuente = new BitmapFont(); // Fuente para la puntuación
        fuente.setColor(Color.WHITE); // Color blanco para la puntuación
        fuente.getData().setScale(10); // Tamaño de la fuente

        // Inicialización de las texturas de los pájaros
        pajaro = new Texture[2];
        pajaro[0] = new Texture("pajaro1.png");
        pajaro[1] = new Texture("pajaro2.png");

        // Inicialización de los tubos
        tuboArriba = new Texture("tuberia_arriba.png");
        tuboAbajo = new Texture("tuberia_abajo.png");
        desplazamientoMaxTubo = Gdx.graphics.getHeight() / 2 - espacioTuberias / 2 - 100; // Ajuste de la altura máxima de los tubos
        generadorAleatorio = new Random(); // Inicializa el generador aleatorio
        distanciaEntreTuberias = Gdx.graphics.getWidth() * 3 / 4; // Distancia entre tubos
        rectangulosTuboArriba = new Rectangle[cantidadTuberias];
        rectangulosTuboAbajo = new Rectangle[cantidadTuberias];

        iniciarJuego(); // Inicializa el estado del juego
    }

    // Función que inicializa la posición del pájaro y los tubos
    public void iniciarJuego() {
        posYPajaro = Gdx.graphics.getHeight() / 2 - pajaro[0].getHeight() / 2; // Inicializa el pájaro en el centro vertical

        // Posiciona las tuberías
        for (int i = 0; i < cantidadTuberias; i++) {
            desplazamientoTuberias[i] = (generadorAleatorio.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - espacioTuberias - 200); // Ajusta la posición aleatoria de los tubos en Y
            posXTuberias[i] = Gdx.graphics.getWidth() / 2 - tuboArriba.getWidth() / 2 + Gdx.graphics.getWidth() + i * distanciaEntreTuberias; // Ajusta la posición inicial de los tubos
            rectangulosTuboArriba[i] = new Rectangle(); // Crea el rectángulo para el tubo superior
            rectangulosTuboAbajo[i] = new Rectangle(); // Crea el rectángulo para el tubo inferior
        }
    }

    @Override
    public void render () {
        loteSprites.begin(); // Comienza el renderizado de los elementos gráficos

        // Dibuja el fondo en pantalla
        loteSprites.draw(fondo, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Si el juego está en el estado "jugando"
        if (estadoJuego == 1) {

            // Lógica para aumentar la puntuación cuando el pájaro pasa por un tubo
            if (posXTuberias[tuboPuntuacion] < Gdx.graphics.getWidth() / 2) {
                puntuacion++; // Aumenta la puntuación
                Gdx.app.log("Puntuación", String.valueOf(puntuacion)); // Muestra la puntuación en los logs
                if (tuboPuntuacion < cantidadTuberias - 1) {
                    tuboPuntuacion++; // Si no es el último tubo, pasa al siguiente
                } else {
                    tuboPuntuacion = 0; // Si es el último tubo, vuelve al primero
                }
            }

            // Controla el salto del pájaro cuando el jugador toca la pantalla
            if (Gdx.input.justTouched()) {
                velocidad = -30; // Establece una velocidad negativa para el salto del pájaro
            }

            // Mueve y dibuja los tubos
            for (int i = 0; i < cantidadTuberias; i++) {
                // Si el tubo ya ha pasado la pantalla, lo reposiciona al final
                if (posXTuberias[i] < -tuboArriba.getWidth()) {
                    posXTuberias[i] += cantidadTuberias * distanciaEntreTuberias;
                    desplazamientoTuberias[i] = (generadorAleatorio.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - espacioTuberias - 200);
                } else {
                    posXTuberias[i] = posXTuberias[i] - velocidadTubo; // Mueve los tubos hacia la izquierda
                }

                // Dibuja los tubos en pantalla
                loteSprites.draw(tuboArriba, posXTuberias[i], Gdx.graphics.getHeight() / 2 + espacioTuberias / 2 + desplazamientoTuberias[i]);
                loteSprites.draw(tuboAbajo, posXTuberias[i], Gdx.graphics.getHeight() / 2 - espacioTuberias / 2 - tuboAbajo.getHeight() + desplazamientoTuberias[i]);

                // Actualiza los rectángulos de los tubos para detectar colisiones
                rectangulosTuboArriba[i] = new Rectangle(posXTuberias[i], Gdx.graphics.getHeight() / 2 + espacioTuberias / 2 + desplazamientoTuberias[i], tuboArriba.getWidth(), tuboArriba.getHeight());
                rectangulosTuboAbajo[i] = new Rectangle(posXTuberias[i], Gdx.graphics.getHeight() / 2 - espacioTuberias / 2 - tuboAbajo.getHeight() + desplazamientoTuberias[i], tuboAbajo.getWidth(), tuboAbajo.getHeight());
            }

            // Actualiza la física del pájaro
            if (posYPajaro > 0) {
                velocidad = velocidad + gravedad; // Aplica la gravedad
                posYPajaro -= velocidad; // Mueve al pájaro hacia abajo
            } else {
                estadoJuego = 2; // Si el pájaro toca el suelo, termina el juego
            }

        } else if (estadoJuego == 0) { // Si el juego está esperando a que empiece
            if (Gdx.input.justTouched()) {
                estadoJuego = 1; // Comienza el juego
            }

        } else if (estadoJuego == 2) { // Si el juego terminó
            loteSprites.draw(finJuego, Gdx.graphics.getWidth() / 2 - finJuego.getWidth() / 2, Gdx.graphics.getHeight() / 2 - finJuego.getHeight() / 2);

            if (Gdx.input.justTouched()) {
                estadoJuego = 1; // Reinicia el juego al tocar la pantalla
                iniciarJuego(); // Inicializa nuevamente el juego
                puntuacion = 0; // Resetea la puntuación
                tuboPuntuacion = 0; // Resetea el contador de tubos
                velocidad = 0; // Resetea la velocidad
            }
        }

        // Alterna entre los dos estados del pájaro para simular el aleteo
        if (estadoAleteo == 0) {
            estadoAleteo = 1;
        } else {
            estadoAleteo = 0;
        }

        // Dibuja el pájaro
        loteSprites.draw(pajaro[estadoAleteo], Gdx.graphics.getWidth() / 2 - pajaro[estadoAleteo].getWidth() / 2, posYPajaro);
        fuente.draw(loteSprites, String.valueOf(puntuacion), 100, 200); // Muestra la puntuación en pantalla

        // Actualiza la posición del círculo del pájaro para detectar colisiones
        circuloPajaro.set(Gdx.graphics.getWidth() / 2, posYPajaro + pajaro[estadoAleteo].getHeight() / 2, pajaro[estadoAleteo].getWidth() / 2);

        // Detecta colisiones con los tubos
        for (int i = 0; i < cantidadTuberias; i++) {
            if (Intersector.overlaps(circuloPajaro, rectangulosTuboArriba[i]) || Intersector.overlaps(circuloPajaro, rectangulosTuboAbajo[i])) {
                estadoJuego = 2; // Si hay colisión, termina el juego
            }
        }
        loteSprites.end(); // Finaliza el renderizado de los elementos gráficos
    }
}
