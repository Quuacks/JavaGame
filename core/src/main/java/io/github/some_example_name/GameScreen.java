package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameScreen implements Screen {
    Main game;

    public Tilemap tilemap;
    public Texture tileset;
    public Texture playerMain;
    public Texture playerSpriteSheet;
    public Texture coinSpriteSheet;

    public Player player;

    private OrthographicCamera camera;
    private ExtendViewport viewport;
    int tileSize = 16 * 4;

    public SpriteBatch spriteBatch;
    private Stage stage;

    OrthographicCamera uiCamera;

    public BitmapFont textFieldFont;
    BitmapFont tileInfoText;

    boolean isNextLevelLoading = false;
    int currentLevel = 0;

    public GameScreen(Main game, int level){
        this.game = game;
        currentLevel = level;
    }

    @Override public void show(){
        //same as create

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        viewport.apply();

        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        tileset = new Texture(Gdx.files.internal("world_tileset.png"));
        coinSpriteSheet = new Texture(Gdx.files.internal("coin.png"));
        tilemap = new Tilemap(tileset, coinSpriteSheet, tileSize, camera);

        spriteBatch = new SpriteBatch();

        playerMain = new Texture(Gdx.files.internal("PlayerMain.png"));
        playerSpriteSheet = new Texture(Gdx.files.internal("PlayerRun.png"));

        player = new Player(playerMain, playerSpriteSheet, tilemap);

        stage = new Stage(new ScreenViewport());

        if(currentLevel >= game.maps.size){
            currentLevel = 0;
        }
        tilemap.LoadMap(game.maps.get(currentLevel));

        textFieldFont = new BitmapFont();
        textFieldFont.setColor(Color.BLACK);

        tileInfoText = new BitmapFont();
        tileInfoText.setColor(Color.BLACK);
    }

    @Override
    public void render(float delta){
        ScreenUtils.clear(1,1,1,1);
        // gameplay
        Input();

        player.ApplyGravity(delta);
        player.ApplyVelocity(delta);

        camera.position.set(new Vector3(player.position.x, player.position.y, 0f));
        camera.update();

        spriteBatch.setProjectionMatrix(camera.combined);

        spriteBatch.begin();

        tilemap.Draw(spriteBatch);
        DrawPlayer();
        tilemap.CheckCoinCollision(player);
        if(tilemap.CheckForGameEnd(player))
            LoadNextLevel();
        spriteBatch.end();

        spriteBatch.setProjectionMatrix(uiCamera.combined);

        spriteBatch.begin();

        player.PrintCollisionBelow(spriteBatch, tileInfoText);

        textFieldFont.setColor(Color.BLACK);
        textFieldFont.draw(spriteBatch,
            "Coins: " + player.coinCount + " / " + tilemap.coinsPlaced,
            20,
            Gdx.graphics.getHeight() - 20);
        spriteBatch.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override public void resize(int w,int h){
        viewport.update(w,h);
        uiCamera.setToOrtho(false, w, h);
    }
    @Override public void pause(){}
    @Override public void resume(){}
    @Override public void hide(){}
    @Override public void dispose(){
        spriteBatch.dispose();
        tileset.dispose();
        playerMain.dispose();
        playerSpriteSheet.dispose();
        stage.dispose();
    }

    public void Input(){
        if(Gdx.input.isKeyPressed(Input.Keys.SPACE)){
            player.Jump();
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A)){
            player.velocity.x -= player.speed;
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.D)){
            player.velocity.x += player.speed;
        }
        else
            player.velocity.x = 0;
//        if(Gdx.input.isKeyPressed(Input.Keys.W)){
//            player.velocity.y += player.speed;
//        }

//        if(Gdx.input.isKeyPressed(Input.Keys.S)){
//            player.velocity.y -= player.speed;
//        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.K)){
            LoadNextLevel();
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            game.setScreen(new MainMenuScreen(game));
        }
    }
    public void DrawPlayer(){
        float  delta = Gdx.graphics.getDeltaTime();

        player.ApplyGravity(delta);
        player.ApplyVelocity(delta);
        spriteBatch.draw(player.textureMain, player.position.x, player.position.y, player.textureMain.getRegionWidth() * player.playerSizeMult, player.textureMain.getRegionHeight() * player.playerSizeMult);
    }
    public void LoadNextLevel(){
        if(isNextLevelLoading)
            return;
        else
            isNextLevelLoading = true;
        ++currentLevel;
        if(currentLevel >= game.maps.size){
            currentLevel = 0;
        }

        game.setScreen(new GameScreen(game, currentLevel));
    }

}
