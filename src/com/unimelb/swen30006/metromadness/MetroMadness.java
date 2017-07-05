package com.unimelb.swen30006.metromadness;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;

/**
 * The MetroMadness class is an ApplicationAdapter
 * for the simulation. It delegates appropriate system events
 * to Simulation, the facade controller.
 */
public class MetroMadness extends ApplicationAdapter {

    /** The simulation's width in unitless dimensions. */
    private static final int WORLD_WIDTH = 1200;
    /** The simulation's height in unitless dimensions. */
    private static final int WORLD_HEIGHT = 1200;

    /** Initial width of the viewport (application window). */
    private int VIEWPORT_WIDTH = 200;
    /** Current width of the viewport (application window). */
    private float viewport_width;

    /** Out instance of Simulation, the facade controller. */
    private Simulation simultaion;
    /** ShapeRenderer used to render each shape to the screen. */
    private ShapeRenderer shapeRenderer;
    /** The camera used to set which part of the world is viewable. */
    private OrthographicCamera camera;
    
    /** The small font used in the simulation. */
    private BitmapFont smaller;
    /** The header font used in the simulation. */
    private BitmapFont header;

    /**
     * Resizes the viewport to a given width and height.
     * 
     * @param width the width to resize to
     * @param height the height to resize to
     */
    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = viewport_width;
        camera.viewportHeight = viewport_width * (float)height/width;
        camera.update();
    }

    /**
     * Create the MetroMaddness ApplicationAdapter, including initialising
     * the simulation.
     */
    @Override
    public void create() {
        // Create the simulation
        simultaion = new Simulation("../core/assets/maps/melbourne.xml");       
        
        // Setup our 2D Camera
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        viewport_width = VIEWPORT_WIDTH;
        camera = new OrthographicCamera(viewport_width,
                viewport_width * (height / width));
        camera.position.set(camera.viewportWidth / 2f,
                            camera.viewportHeight / 2f, 0);
        camera.update();
        
        // Create our shape renderer
        shapeRenderer = new ShapeRenderer();
        
        // Create our font
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(
                Gdx.files.internal("assets/fonts/Gotham-Book.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        // font size 12 pixels
        parameter.size = 12;
        smaller = generator.generateFont(parameter);
        // don't forget to dispose to avoid memory leaks!
        generator.dispose();

        FreeTypeFontGenerator headlineGen =
            new FreeTypeFontGenerator(
                Gdx.files.internal("assets/fonts/Gotham-Bold.ttf"));
        FreeTypeFontParameter headlineParam = new FreeTypeFontParameter();
        // font size 40 pixels
        headlineParam.size = 40;
        header = headlineGen.generateFont(headlineParam);
        // don't forget to dispose to avoid memory leaks!
        headlineGen.dispose();
        
        // Setup fonts
        smaller.setColor(Color.GRAY);
        header.setColor(Color.BLACK);
    }

    /**
     * Render the screen, called once per frame, delegates rendering on
     * screen objects to Simulation.
     */
    @Override
    public void render () {
        // Clear the graphics to white
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Handle user input
        handleInput();
        
        // Update the simulation and camera
        camera.update();
        simultaion.update();
        
        // Render the simulation
        shapeRenderer.setProjectionMatrix(camera.combined);
         
        // Render all filled shapes.
        shapeRenderer.begin(ShapeType.Filled);
        simultaion.render(shapeRenderer);
        shapeRenderer.end();
         
        // Begin preparations to render text
        SpriteBatch batch = new SpriteBatch();
        batch.begin();
        
        // Render Header
        header.getData().setScale(0.5f);
        header.draw(batch, "metro madness.", 10,
                    Gdx.graphics.getHeight() - 10);
        batch.end();
    }
    
    /**
     * Handle the keyboard input, dealing with zooming, panning and exiting.
     */
    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.zoom += 0.1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.zoom -= 0.1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            camera.translate(-3f, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            Gdx.app.exit();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.translate(3f, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.translate(0, -3f, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.translate(0, 3f, 0);
        }

        
        camera.zoom = MathUtils.clamp(camera.zoom, 0.1f,
                                      WORLD_WIDTH  /camera.viewportWidth);
        float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
        float effectiveViewportHeight = camera.viewportHeight * camera.zoom;

        camera.position.x = MathUtils.clamp(camera.position.x,
            effectiveViewportWidth / 2f,
            WORLD_WIDTH - effectiveViewportWidth / 2f);
        camera.position.y = MathUtils.clamp(camera.position.y,
            effectiveViewportHeight / 2f,
            WORLD_HEIGHT - effectiveViewportHeight / 2f);
    }

}
