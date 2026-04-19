package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class Player {
    Tilemap tilemap;

    public int coinCount;

    float speed = 20f;
    int playerSizeMult = 4;
    float jumpSpeed = 200f;
    float drag = 25f;
    float maxVelocity = 175f;
    float gravity = 15f;
    float riseMultiplier = 12f;
    float fallMultiplier = 10f;

    boolean onGround = true;

    Vector2 position;
    Vector2 velocity = new Vector2(0f, 0f);

    GridPoint2 borderX;
    GridPoint2 borderY;

    TextureRegion textureMain;
    Texture animationTextureSpriteSheet;
    Texture[] animationTextures;
    public Player(Texture textureMain, Texture spriteSheet, Tilemap tilemap){
        position = new Vector2(0,0);
        animationTextureSpriteSheet = spriteSheet;
        this.borderX = new GridPoint2(8, textureMain.getWidth() * playerSizeMult - 8);
        this.borderY = new GridPoint2(2, textureMain.getHeight() * playerSizeMult - 2);
        this.textureMain = new TextureRegion(textureMain);
        this.tilemap = tilemap;
        coinCount = 0;
    }
    public void CoinCollected(){
        ++coinCount;
        if(coinCount >= tilemap.coinsPlaced){

        }
    }
    public void ApplyVelocity(float delta){
        //System.out.println(velocity.x);
        velocity.x = Math.max(-maxVelocity, Math.min(maxVelocity, velocity.x));
        if(velocity.x < 0 && !textureMain.isFlipX()){
            textureMain.flip(true, false);
        }
        else if(velocity.x > 0 && textureMain.isFlipX())
            textureMain.flip(true, false);

        velocity.y = Math.max(-maxVelocity, Math.min(maxVelocity, velocity.y));

        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        ApplyDragY(delta);
        CheckCollision();
    }
    void ApplyDragY(float delta){
        if(onGround)
            return;

        if(velocity.y > 0)
            velocity.y = Math.max(0, velocity.y - (drag * delta));
        else
            velocity.y = Math.min(0, velocity.y - (drag * 4) * delta);
    }
    public void Jump() {
        if (onGround) {   // only jump if on ground
            velocity.y = jumpSpeed;
            onGround = false;
        }
    }

    public void ApplyGravity(float delta) {
        if(onGround)
            return;

        float g = gravity;

        // rising
        if (velocity.y > 0)
            g *= riseMultiplier;

        // falling
        if (velocity.y < 0)
            g *= fallMultiplier;

        velocity.y -= g * delta;

        position.y += velocity.y * delta;

        CheckCollision();
    }
    public void CheckCollision(){

        int playerLeft = (int)Math.floor((position.x + borderX.x) / 64f);
        int playerRight = (int)Math.floor((position.x + borderX.y) / 64f);
        int playerTop = (int)Math.floor((position.y + borderY.y) / 64f);
        int playerBottom = (int)Math.floor((position.y + borderY.x) / 64f);

        int tileBelow = playerBottom - 1;
        int tileLeft = playerLeft;
        int tileRight = playerRight;
        int tileTop = playerTop;

        if(IsCollisionBelow(tileLeft, tileRight, tileBelow) && velocity.y <= 0){
            velocity.y = 0;
            position.y = (tileBelow + 1) * 64 - borderY.x;
            onGround = true;

        }
        else
            onGround = false;

        if(IsCollisionTop(tileLeft, tileRight, tileTop)){
            velocity.y = 0;
            position.y = tileTop * 64 - borderY.y;
        }

        if(IsCollisionLeft((int)((position.y + borderY.y - 16) / 64), (int)((position.y + borderY.x + 16) / 64), tileLeft)){
            velocity.x = 0;
            position.x = (tileLeft + 1) * 64 - borderX.x;
        }
        else if(IsCollisionRight((int)((position.y + borderY.y - 16) / 64), (int)((position.y + borderX.x + 16) / 64), tileRight)){
            velocity.x = 0;
            position.x = tileRight * 64 - borderX.y;
        }

    }
    public boolean IsCollisionBelow(int playerLeft, int playerRight, int tileBelow){

        if(tilemap.tilesToDraw.containsKey(new GridPoint2(playerLeft, tileBelow)) || tilemap.tilesToDraw.containsKey(new GridPoint2(playerRight, tileBelow))){
            if(Math.abs(position.y - ((tileBelow + 1) * 64)) < 4f){
                //System.out.println("Collison: " + tileBelow + " player: " + position.y);
                return true;
            }
        }
        return false;
    }
    public void PrintCollisionBelow(SpriteBatch spriteBatch, BitmapFont text){
        int playerLeft = (int)Math.floor((position.x + borderX.x) / 64f);
        int playerRight = (int)Math.floor((position.x + borderX.y) / 64f);
        int playerBottom = (int)Math.floor((position.y + borderY.x) / 64f);

        int tileBelow = playerBottom - 1;
        if(tilemap.tilesToDraw.containsKey(new GridPoint2(playerLeft, tileBelow)) || tilemap.tilesToDraw.containsKey(new GridPoint2(playerRight, tileBelow))){
            if(Math.abs(position.y - ((tileBelow + 1) * 64)) < 4f){
                Tile tile = tilemap.tilesToDraw.get(new GridPoint2(playerLeft, tileBelow));
                if(tile == null)
                    tile = tilemap.tilesToDraw.get(new GridPoint2(playerRight, tileBelow));
                //System.out.println("Collison: " + tileBelow + " player: " + position.y);
                text.draw(spriteBatch, "" + tile.tileID, 20, Gdx.graphics.getHeight() - 40);
            }
        }

    }
    boolean IsCollisionTop(int playerLeft, int playerRight, int tileAbove){

        if(tilemap.tilesToDraw.containsKey(new GridPoint2(playerLeft, tileAbove)) || tilemap.tilesToDraw.containsKey(new GridPoint2(playerRight, tileAbove))){
            if(Math.abs((position.y + borderY.y) - (tileAbove * 64)) < 4f){
                return true;
            }
        }
        return false;
    }
    public boolean IsCollisionLeft(int playerTop, int playerBottom, int tileLeft){

        if(tilemap.tilesToDraw.containsKey(new GridPoint2(tileLeft, playerBottom)) ||
           tilemap.tilesToDraw.containsKey(new GridPoint2(tileLeft, playerTop))){

            float wallLeft = (tileLeft + 1) * 64;
            if(position.x <= wallLeft){
                return true;
            }
        }
        return false;
    }
    boolean IsCollisionRight(int playerTop, int playerBottom, int tileRight){

        if(tilemap.tilesToDraw.containsKey(new GridPoint2(tileRight, playerBottom)) ||
            tilemap.tilesToDraw.containsKey(new GridPoint2(tileRight, playerTop))){

            float wallRight = tileRight * 64;
            if((position.x + borderX.y) >= wallRight){
                return true;
            }
        }
        return false;
    }
}
