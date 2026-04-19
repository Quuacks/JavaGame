package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class Tile
{
    TextureRegion texture;
    int posX;
    int posY;
    int tileSize;
    int tileID;
    boolean hasCollision;

    public Tile(TextureRegion texture, int posX, int posY, int tileSize, int tileID){
        this.texture = texture;
        this.posX = posX;
        this.posY = posY;
        this.tileSize = tileSize;
        this.tileID = tileID;
    }

    public void DrawTile(SpriteBatch spriteBatch){
        spriteBatch.draw(texture, posX, posY, tileSize, tileSize);
    }

}
