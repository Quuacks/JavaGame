package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class CoinData{
    public Vector2 pos;
    Texture coinSpriteSheet;
    Array<TextureRegion> frame = new Array<TextureRegion>();
    Animation<TextureRegion> runAnimation;
    float stateTime = 0f;
    public CoinData(Vector2 pos, Texture coinSpriteSheet){
        this.pos = pos;
        this.coinSpriteSheet = coinSpriteSheet;
        ProcessSpriteSheet();
        runAnimation = new Animation<>(0.1f, frame);
    }

    public boolean Interact(Player player){
        if(player.position.dst(pos) <= 32f){
            player.coinCount++;
            return true;
        }
        return false;
    }
    void ProcessSpriteSheet(){
        TextureRegion[][] tmpFrames = TextureRegion.split(coinSpriteSheet, 16,16);

        int index = 0;
        for(int y = 0; y < tmpFrames.length; y++){
            for(int x = 0; x < tmpFrames[0].length; x++){
                frame.add(tmpFrames[y][x]);
            }
        }
    }
    public void DrawCoinAnimation(SpriteBatch spriteBatch, float delta){
        stateTime += delta;

        TextureRegion frame = runAnimation.getKeyFrame(stateTime, true);
        spriteBatch.draw(frame, pos.x, pos.y, 64, 64);
    }
    public void DrawCoin(SpriteBatch spriteBatch){
        spriteBatch.draw(frame.get(0), pos.x, pos.y, 64, 64);
    }
}
