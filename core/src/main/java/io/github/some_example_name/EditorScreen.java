package io.github.some_example_name;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class EditorScreen implements Screen, InputProcessor {
    Main game;

    public Tilemap tilemap;
    public Texture tileset;
    public Texture coinTexture;

    private OrthographicCamera camera;
    private ExtendViewport viewport;
    public SpriteBatch spriteBatch;

    private Stage stage;

    public Table popUpBar;
    public TextField fileNameInputField;

    public Texture downTex;
    public Texture upTex;
    public Texture paintButtonDown;
    public Texture paintButtonUp;
    public Texture eraseButtonUp;
    public Texture eraseButtonDown;
    public Texture coinButtonUp;
    public Texture coinButtonDown;
    public Texture confirmButtonUp;
    public Texture confirmButtonDown;

    boolean eraseTiles = false;
    boolean placeCoin;
    boolean exportMap;

    int buttonsPerRow = 24;
    int tileSelected = -1;

    private Vector3 lastMousePos = new Vector3();
    private boolean isPanning = false;

    int tileSize = 16 * 4;

    public EditorScreen(Main game){
        this.game = game;
    }

    @Override
    public void show(){
        // your current create() code
        spriteBatch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);

        viewport.apply();

        tileset = new Texture(Gdx.files.internal("world_tileset.png"));
        coinTexture = new Texture(Gdx.files.internal("coin.png"));
        tilemap = new Tilemap(tileset, coinTexture, tileSize, camera);

//        maps.add("Map1.json");
//        maps.add("Map2.json");
//        maps.add("Map3.json");
//        maps.add("Map4.json");
//        maps.add("Map5.json");

        upTex = new Texture(Gdx.files.internal("ui/LongButtonDown.png"));
        downTex = new Texture(Gdx.files.internal("ui/LongButtonDown.png"));
        paintButtonUp = new Texture(Gdx.files.internal("ui/PaintButton.png"));
        paintButtonDown = new Texture(Gdx.files.internal("ui/PaintButtonDown.png"));
        eraseButtonDown = new Texture(Gdx.files.internal("ui/EraseButtonDown.png"));
        eraseButtonUp = new Texture(Gdx.files.internal("ui/EraseButton.png"));
        coinButtonDown = new Texture(Gdx.files.internal("ui/CollisionButtonDown.png"));
        coinButtonUp = new Texture(Gdx.files.internal("ui/CollisionButton.png"));
        confirmButtonDown = new Texture(Gdx.files.internal("ui/ConfirmButtonDown.png"));
        confirmButtonUp = new Texture(Gdx.files.internal("ui/ConfirmButton.png"));


        stage = new Stage(new ScreenViewport());

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(this);

        Gdx.input.setInputProcessor(multiplexer);

        createUI();

        tilemap.LoadMap("MapCollision1.json");
    }

    @Override
    public void render(float delta){
        // your current render() code
        ScreenUtils.clear(1,1,1,1);

        updateCamera();

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        tilemap.Draw(spriteBatch);

        if(!eraseTiles && tileSelected != -1)
            previewTile();

        Input();

        spriteBatch.end();

        // THIS PART IS REQUIRED
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w,int h){
        viewport.update(w, h);
        stage.getViewport().update(w, h, true);
    }
    @Override public void pause(){}
    @Override public void resume(){}
    @Override public void hide(){}
    @Override public void dispose(){}

    public void Input(){
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            game.setScreen(new MainMenuScreen(game));
        }
    }
    public void createUI()
    {
        Table bottomBar = new Table();
        Table topRightTable = new Table();
        Table topBar = new Table();
        popUpBar = new Table();

        popUpBar.setFillParent(true);
        popUpBar.center();

        topBar.setFillParent(true);
        topBar.top();

        bottomBar.setFillParent(true);
        bottomBar.bottom();
        bottomBar.setHeight(buttonsPerRow * tileSize);

        topRightTable.setFillParent(true);
        topRightTable.top().right();

        stage.addActor(topRightTable);
        stage.addActor(bottomBar);
        stage.addActor(topBar);
        stage.addActor(popUpBar);

        for(int i = 0; i < tilemap.tiles.length; i++)
        {
            createButton(bottomBar, tilemap.tiles[i], i);
        }

        BitmapFont font = new BitmapFont();
        font.setColor(Color.BLACK);

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = new TextureRegionDrawable(new TextureRegion(upTex));
        textButtonStyle.down = new TextureRegionDrawable(new TextureRegion(downTex));
        textButtonStyle.over = textButtonStyle.up;
        textButtonStyle.font = font;

        TextButton importButton = new TextButton("Import", textButtonStyle);
        TextButton exportButton = new TextButton("Export", textButtonStyle);

        exportButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                ExportMap();
            }
        });

        importButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                ImportMap();
            }
        });

        topRightTable.add(exportButton).pad(10f);
        topRightTable.add(importButton).pad(10f);

        TextureRegionDrawable buttonPaintUp = new TextureRegionDrawable(paintButtonUp);
        TextureRegionDrawable buttonPaintDown = new TextureRegionDrawable(paintButtonDown);
        TextureRegionDrawable buttonEraseUp = new TextureRegionDrawable(eraseButtonUp);
        TextureRegionDrawable buttonEraseDown = new TextureRegionDrawable(eraseButtonDown);
        TextureRegionDrawable buttonCoinUp = new TextureRegionDrawable(coinButtonUp);
        TextureRegionDrawable buttonCoinDown = new TextureRegionDrawable(coinButtonDown);
        TextureRegionDrawable buttonConfirmUp = new TextureRegionDrawable(confirmButtonUp);
        TextureRegionDrawable buttonConfirmDown = new TextureRegionDrawable(confirmButtonDown);

        ImageButton.ImageButtonStyle paintButtonStyle = new ImageButton.ImageButtonStyle();
        ImageButton.ImageButtonStyle eraseButtonStyle = new ImageButton.ImageButtonStyle();
        ImageButton.ImageButtonStyle coinButtonStyle = new ImageButton.ImageButtonStyle();
        ImageButton.ImageButtonStyle confirmButtonStyle = new ImageButton.ImageButtonStyle();

        paintButtonStyle.up = buttonPaintUp;
        paintButtonStyle.down = buttonPaintDown;
        eraseButtonStyle.up = buttonEraseUp;
        eraseButtonStyle.down = buttonEraseDown;
        coinButtonStyle.up = buttonCoinUp;
        coinButtonStyle.down = buttonCoinDown;
        confirmButtonStyle.up = buttonConfirmUp;
        confirmButtonStyle.down = buttonConfirmDown;

        ImageButton paintButton = new ImageButton(paintButtonStyle);
        ImageButton eraseButton = new ImageButton(eraseButtonStyle);
        ImageButton coinButton = new ImageButton(coinButtonStyle);
        ImageButton confirmButton = new ImageButton(confirmButtonStyle);

        paintButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                ChangePaintType(false);
            }
        });
        eraseButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                ChangePaintType(true);
            }
        });
        coinButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                CoinPlacement();
            }
        });
        confirmButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                ConfirmChoice();
            }
        });

        topBar.add(paintButton);
        topBar.add(eraseButton);
        topBar.add(coinButton);

        BitmapFont textFieldFont = new BitmapFont();
        textFieldFont.setColor(Color.BLACK);

        TextureRegionDrawable background =
            new TextureRegionDrawable(new TextureRegion(upTex));

        TextureRegionDrawable cursor =
            new TextureRegionDrawable(new TextureRegion(downTex)); // or a thin white texture

        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = textFieldFont;
        textFieldStyle.fontColor = Color.BLACK;
        textFieldStyle.background = background;
        textFieldStyle.cursor = cursor;

        fileNameInputField = new TextField("", textFieldStyle);
        fileNameInputField.setMessageText("File name...");
        fileNameInputField.setWidth(300);

        fileNameInputField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {

            }
        });

        fileNameInputField.setWidth(300);
        fileNameInputField.setHeight(100);
        popUpBar.add(fileNameInputField).pad(10);
        popUpBar.add(confirmButton);
        popUpBar.setVisible(false);

    }
    public void ChangePaintType(boolean eraseMode){
        eraseTiles = eraseMode;
        placeCoin = false;
    }
    public void CoinPlacement(){
        placeCoin = !placeCoin;
        eraseTiles = false;
    }
    public void createButton(Table parent, TextureRegion texture, int arg)
    {
        TextureRegionDrawable drawable = new TextureRegionDrawable(texture);
        drawable.setMinSize(tileSize,tileSize);
        TextureRegionDrawable downDrawable = new TextureRegionDrawable(texture);
        downDrawable.tint(Color.GRAY);
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = drawable;
        style.down = downDrawable;
        ImageButton button = new ImageButton(style);

        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                tileSelected(arg);
            }
        });

        parent.add(button).expandX();
        if((arg + 1) % buttonsPerRow == 0){
            parent.row();
        }

    }
    private void tileSelected(int tileID)
    {
        tileSelected = tileID;
    }
    public void previewTile()
    {
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);

        int intX = MathUtils.floor(mousePos.x / tileSize) * tileSize;
        int intY = MathUtils.floor(mousePos.y / tileSize) * tileSize;

        spriteBatch.setColor(1,1,1,0.5f);

        spriteBatch.draw(tilemap.tiles[tileSelected], intX, intY, tileSize, tileSize);
        spriteBatch.setColor(1,1,1,1);
    }
    public void ExportMap(){
        //spawm Ui for file name input;
        exportMap = true;
        popUpBar.setVisible(true);
    }
    public void ImportMap(){
        exportMap = false;
        popUpBar.setVisible(true);
    }
    public void ConfirmChoice(){
        String fileName = fileNameInputField.getText();
        if(exportMap)
            tilemap.SaveMap(fileName);
        else
            tilemap.LoadMap(fileName);
        popUpBar.setVisible(false);
    }
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {

            Vector3 world = new Vector3(screenX, screenY, 0);
            viewport.unproject(world);

            int gridX = MathUtils.floor(world.x / tileSize) * tileSize;
            int gridY = MathUtils.floor(world.y / tileSize) * tileSize;

            if(eraseTiles)
                tilemap.PlaceTile(gridX, gridY, -1);
            else if(placeCoin){
                tilemap.PlaceCoin(gridX, gridY);
            }
            else if(tileSelected != -1)
                tilemap.PlaceTile(gridX, gridY, tileSelected);

            return true; // consumed
        }

        return false;
    }
    private void updateCamera()
    {
        if(Gdx.input.isButtonPressed(Input.Buttons.RIGHT))
        {
            Vector3 currentMousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            //currentMousePos.add(-(Gdx.graphics.getWidth() / 2), -(Gdx.graphics.getHeight() / 2), 0);

            viewport.unproject(currentMousePos);
            if(isPanning)
            {
                float dx = lastMousePos.x - currentMousePos.x;
                float dy = lastMousePos.y - currentMousePos.y;

                camera.translate(dx * 0.5f, dy * 0.5f);
            }

            lastMousePos.set(currentMousePos);
            isPanning = true;
        }
        else
        {
            isPanning = false;
        }
        camera.update();
    }

    @Override
    public boolean keyDown(int i) {
        return false;
    }

    @Override
    public boolean keyUp(int i) {
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        return false;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean scrolled(float v, float v1) {
        return false;
    }
}
