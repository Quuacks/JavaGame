package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainMenuScreen implements Screen {

    private Main game;
    private Stage stage;

    public MainMenuScreen(Main game){
        this.game = game;
        stage = new Stage(new ScreenViewport());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        BitmapFont font = new BitmapFont();
        font.setColor(Color.BLACK);

        Texture upTex = new Texture(Gdx.files.internal("ui/LongButtonDown.png"));
        Texture downTex = new Texture(Gdx.files.internal("ui/LongButtonDown.png"));

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = new TextureRegionDrawable(new TextureRegion(upTex));
        textButtonStyle.down = new TextureRegionDrawable(new TextureRegion(downTex));
        textButtonStyle.over = textButtonStyle.up;
        textButtonStyle.font = font;

        TextButton startGame = new TextButton("StartGame", textButtonStyle);
        TextButton editor = new TextButton("Editor", textButtonStyle);

        startGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                StartGame();
            }
        });

        editor.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                StartEditor();
            }
        });

        table.add(startGame).pad(20).row();
        table.add(editor).pad(20);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    void StartGame(){
        game.setScreen(new GameScreen(game, 0));
    }

    void StartEditor(){
        game.setScreen(new EditorScreen(game));
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0,0,0,1);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w,int h){ stage.getViewport().update(w,h,true);}
    @Override public void dispose(){ stage.dispose();}
    @Override public void hide(){}
    @Override public void pause(){}
    @Override public void resume(){}
}
