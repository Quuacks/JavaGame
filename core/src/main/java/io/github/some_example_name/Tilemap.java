package io.github.some_example_name;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Tilemap {
    public Texture tileset;
    TextureRegion[] tiles;
    int tileSize;
    int coinsPlaced;
    OrthographicCamera camera;
    ShapeRenderer shapeRenderer;
    Map<GridPoint2, Tile> tilesToDraw = new HashMap<GridPoint2, Tile>();
    Map<GridPoint2, CoinData> coins = new HashMap<GridPoint2, CoinData>();
    Texture coinSpriteSheet;

    public Tilemap(Texture texture, Texture coinSpriteSheet, int tileSize, OrthographicCamera camera){
        shapeRenderer = new ShapeRenderer();
        this.tileSize = tileSize;
        tileset = texture;
        this.camera = camera;
        coinsPlaced = 0;
        this.coinSpriteSheet = coinSpriteSheet;
        SetupTiles();
    }
    public void Draw(SpriteBatch spriteBatch)
    {
        //DrawMapBounds(spriteBatch);
        if(tilesToDraw.isEmpty())
            return;
        for(Tile tile : tilesToDraw.values()){
            tile.DrawTile(spriteBatch);
        }

        if(coins.isEmpty())
            return;
        for(CoinData coin : coins.values()){
            coin.DrawCoinAnimation(spriteBatch, Gdx.graphics.getDeltaTime());
        }

    }
    public void SetupTiles(){
        TextureRegion[][] tmpTiles = TextureRegion.split(tileset, 16,16);

        tiles = new TextureRegion[tmpTiles.length * tmpTiles[0].length];
        int index = 0;
        for(int y = 0; y < tmpTiles.length; y++){
            for(int x = 0; x < tmpTiles[0].length; x++){
                tiles[index++] = tmpTiles[y][x];
                }
        }
    }
    public void SpawnTilesOnScreen(SpriteBatch spriteBatch){
        for(int i = 0; i < tileset.getHeight() / 16; i++){
            for(int j = 0; j < tileset.getWidth() / 16; j++){
                spriteBatch.draw(tiles[i * (tileset.getWidth() / 16) + j], i * 32, (tileset.getHeight() / 32 - j) * 32, 32, 32);
            }
        }
    }
    public void PlaceTile(int posX, int posY, int tileID)
    {
        if(tileID == -1){
            Tile tile = new Tile(tiles[0], posX, posY, 64, tileID);
            tilesToDraw.remove(new GridPoint2(posX / 64, posY / 64));
            coins.remove(new GridPoint2(posX / 64, posY / 64));
            return;
        }

        if(tileID < 0 || tileID >= tiles.length)
            return;
        //System.out.println("Mouse Pos: " + posX + " " + posY);
        Tile newTile = new Tile(tiles[tileID], posX, posY, 64, tileID);
        tilesToDraw.put(new GridPoint2(posX / 64, posY / 64), newTile);
    }
    public void PlaceCoin(int posX, int posY){
        if(tilesToDraw.containsKey(new GridPoint2(posX / 64,posY / 64)))
            return;

        CoinData coin = new CoinData(new Vector2(posX, posY), coinSpriteSheet);
        coins.put(new GridPoint2(posX / 64, posY / 64), coin);
        ++coinsPlaced;
    }
    public void CheckCoinCollision(Player player){
        Iterator<Map.Entry<GridPoint2, CoinData>> iterator = coins.entrySet().iterator();

        while(iterator.hasNext()){
            Map.Entry<GridPoint2, CoinData> entry = iterator.next();
            CoinData coin = entry.getValue();

            if(coin.Interact(player)){
                iterator.remove(); // remove collected coin
            }
        }
    }
    public boolean CheckForGameEnd(Player player){
        if(player.coinCount >= coinsPlaced){
            return true;
        }
        return false;
    }
    public MapData toMapData(){
        MapData mapData = new MapData();

        for(Tile tile : tilesToDraw.values()){
            int gridX = tile.posX / tileSize;
            int gridY = tile.posY / tileSize;
            mapData.tileData.add(new TileData(gridX, gridY, tile.hasCollision, tile.tileID));
        }
        for(CoinData coin : coins.values()){
            int gridX = (int) coin.pos.x / tileSize;
            int gridY = (int) coin.pos.y / tileSize;
            mapData.coinData.add(new CoinSaveData(gridX, gridY));
        }
        return mapData;
    }
    public void SaveMap(String fileName){
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        json.setUsePrototypes(false);

        MapData mapData = toMapData();

        String jsonString = json.prettyPrint(mapData);

        FileHandle file = Gdx.files.local(fileName);
        file.writeString(jsonString, false);

    }
    public void LoadMap(String fileName){
        Json json = new Json();
        FileHandle file = Gdx.files.internal(fileName);

        if (!file.exists()) return;

        MapData data = json.fromJson(MapData.class, file.readString());

        if(data == null || data.tileData == null)
            return;

        tilesToDraw.clear();
        coins.clear();

        for (TileData tileData : data.tileData) {
            PlaceTile(tileData.posX * tileSize, tileData.posY * tileSize, tileData.tileID);
        }

        if(data.coinData == null)
            return;

        for (CoinSaveData coinData : data.coinData){
            PlaceCoin(coinData.posX * tileSize, coinData.posY * tileSize);
        }
    }

}
