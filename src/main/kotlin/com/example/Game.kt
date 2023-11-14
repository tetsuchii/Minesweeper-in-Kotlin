package com.example

import com.example.Game.InfoHeader.gameOver
import com.example.Game.InfoHeader.image
import com.example.Game.InfoHeader.reset
import com.example.Game.InfoHeader.setBombs
import javafx.application.Application
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.stage.Stage
import kotlin.random.Random


class Game : Application() {

    companion object {
        private const val WIDTH = 600
        private const val HEIGHT = 600
        private const val TILESIZE = 30
        private val begin = Image(getResource("/begin.png"), TILESIZE.toDouble(), TILESIZE.toDouble(),false,false)
        private val zero = Image(getResource("/0.png"),TILESIZE.toDouble(),TILESIZE.toDouble(),false,false)
        private val one = Image(getResource("/1.png"),TILESIZE.toDouble(),TILESIZE.toDouble(),false,false)
        private val two = Image(getResource("/2.png"),TILESIZE.toDouble(),TILESIZE.toDouble(),false,false)
        private val three = Image(getResource("/3.png"),TILESIZE.toDouble(),TILESIZE.toDouble(),false,false)
        private val four = Image(getResource("/4.png"),TILESIZE.toDouble(),TILESIZE.toDouble(),false,false)
        private val five = Image(getResource("/5.png"),TILESIZE.toDouble(),TILESIZE.toDouble(),false,false)
        private val six = Image(getResource("/6.png"),TILESIZE.toDouble(),TILESIZE.toDouble(),false,false)
        private val seven = Image(getResource("/7.png"),TILESIZE.toDouble(),TILESIZE.toDouble(),false,false)
        private val eight = Image(getResource("/8.png"),TILESIZE.toDouble(),TILESIZE.toDouble(),false,false)
        private val bomb = Image(getResource("/bomb.png"),TILESIZE.toDouble(),TILESIZE.toDouble(),false,false)
        private val flag = Image(getResource("/flag.png"),TILESIZE.toDouble(),TILESIZE.toDouble(),false,false)
        private val smiley = Image(getResource("/smiley.png"),50.0,50.0,false,false)
        private val sadSmiley = Image(getResource("/smileysad.png"),50.0,50.0,false,false)

    }

    private var tilesX = WIDTH / TILESIZE
    private var tilesY = HEIGHT / TILESIZE
    private lateinit var grid : Array<Array<Tile>>
    private var isGameOver = false
    private var firstClick = true


    override fun start(mainStage: Stage) {
        mainStage.title = "Minesweeper"

        val root = GridPane()
        image.setOnMouseClicked {
            initGame(root)
            firstClick=true
            isGameOver=false
            reset()
        }

        initGame(root)
        root.alignment = Pos.CENTER
        root.isGridLinesVisible = false
        root.setOnMouseClicked {
            if(it.button == MouseButton.PRIMARY && !grid[(it.x / TILESIZE).toInt()][(it.y / TILESIZE).toInt()].hasFlag){
                if (firstClick){
                    firstClick=false
                    grid[(it.x / TILESIZE).toInt()][(it.y / TILESIZE).toInt()].isFirstClick=true
                    generateBoard()
                }
                if(grid[(it.x / TILESIZE).toInt()][(it.y / TILESIZE).toInt()].clicked()){
                    showAllBombs()
                    gameOver()
                }
                if(grid[(it.x / TILESIZE).toInt()][(it.y / TILESIZE).toInt()].countedBombs ==0){
                    reveal((it.x/ TILESIZE).toInt(),(it.y/ TILESIZE).toInt())
                }
                isGameEnd()
            }
            if(it.button == MouseButton.SECONDARY && !isGameOver && !grid[(it.x / TILESIZE).toInt()][(it.y / TILESIZE).toInt()].isOpen && !firstClick){
                grid[(it.x / TILESIZE).toInt()][(it.y / TILESIZE).toInt()].flagged()
                isGameEnd()
            }
        }

        val window = BorderPane()
        window.background = Background(BackgroundFill(Color.DARKGRAY, null, null))
        window.bottom = root
        window.top = InfoHeader

        val scene = Scene(window, WIDTH.toDouble(), HEIGHT.toDouble()+50.0)
        mainStage.scene = scene
        mainStage.isResizable=false

        mainStage.show()
    }

    private fun initGame(root : GridPane) {
        grid = Array(tilesX) {
            Array(tilesY) {
                Tile(0, 0,false )
            }
        }
        (0 until tilesY).forEach { y ->
            (0 until tilesX).forEach { x ->
                grid[x][y].posx=x
                grid[x][y].posy=y
                root.add(grid[x][y],x,y)
                grid[x][y].initImage()
            }
        }
    }


    private fun generateBoard() {
        repeat(50) {
            var x: Int = Random.nextInt(tilesX-1)
            var y: Int = Random.nextInt(tilesY-1)
            while (nearToFirstClick(x, y) || grid[x][y].hasBomb) {
                x = Random.nextInt(tilesX-1)
                y = Random.nextInt(tilesY-1)
            }
            grid[x][y].hasBomb=true
        }
        countBombs()
        (0 until tilesY).forEach { y ->
            (0 until tilesX).forEach { x ->
                val tile = grid[x][y]
                if (tile.hasBomb) grid[x][y].image= ImageView(bomb)
                else
                    when (tile.countedBombs) {
                    0 -> tile.image = ImageView(zero)
                    1 -> tile.image = ImageView(one)
                    2 -> tile.image = ImageView(two)
                    3 -> tile.image = ImageView(three)
                    4 -> tile.image = ImageView(four)
                    5 -> tile.image = ImageView(five)
                    6 -> tile.image = ImageView(six)
                    7 -> tile.image = ImageView(seven)
                    else -> tile.image = ImageView(eight)
                }
                tile.initImage()
            }
        }
    }

    private fun nearToFirstClick(x : Int, y: Int): Boolean {
        if(grid[x][y].isFirstClick) return true
        if (x - 1 >= 0 && y - 1 >= 0 && grid[x - 1][y - 1].isFirstClick) return true
        if (x - 1 >= 0 && grid[x - 1][y].isFirstClick) return true
        if (x - 1 >= 0 && y + 1 < tilesY-1 && grid[x - 1][y + 1].isFirstClick) return true
        if (y - 1 >= 0 && grid[x][y - 1].isFirstClick) return true
        if (y + 1 < tilesY-1 && grid[x][y + 1].isFirstClick) return true
        if (x + 1 < tilesX-1 && y - 1 >= 0 && grid[x + 1][y - 1].isFirstClick) return true
        return if (x + 1 < tilesX-1 && grid[x + 1][y].isFirstClick) true
        else x + 1 < tilesX-1 && y + 1 < tilesY-1 && grid[x + 1][y + 1].isFirstClick
    }

    private fun showAllBombs() {
        isGameOver=true
        (0 until tilesY).forEach { y ->
            (0 until tilesX).forEach { x ->
                grid[x][y].showYourself()
            }
        }
    }

    private fun isGameEnd(){
        (0 until tilesX).forEach { x ->
            (0 until tilesY).forEach { y ->
                if((grid[x][y].hasBomb && !grid[x][y].hasFlag) || (!grid[x][y].hasBomb  && !grid[x][y].isOpen))
                    return
            }
        }
        isGameOver=true
       InfoHeader.youWon()

    }

    private fun countBombs() {
        (0 until tilesX).forEach { x ->
            (0 until tilesY).forEach { y ->
                var count = 0
                if (x - 1 >= 0 && y - 1 >= 0 && grid[x - 1][y - 1].hasBomb) count++
                if (x - 1 >= 0 && grid[x - 1][y].hasBomb) count++
                if (x - 1 >= 0 && y + 1 < tilesY && grid[x - 1][y + 1].hasBomb) count++
                if (y - 1 >= 0 && grid[x][y - 1].hasBomb) count++
                if (y + 1 < tilesY && grid[x][y + 1].hasBomb) count++
                if (x + 1 < tilesX && y - 1 >= 0 && grid[x + 1][y - 1].hasBomb) count++
                if (x + 1 < tilesX && grid[x + 1][y].hasBomb) count++
                if (x + 1 < tilesX && y + 1 < tilesY && grid[x + 1][y + 1].hasBomb) count++
                grid[x][y].countedBombs = count
            }
        }
    }

    private fun reveal(x: Int, y: Int) {
        grid[x][y].showYourself()
        if (grid[x][y].countedBombs != 0) return
        if (x - 1 >= 0 && y - 1 >= 0 && grid[x - 1][y - 1].canReveal()) reveal(x - 1, y - 1)
        if (x - 1 >= 0 && grid[x - 1][y].canReveal()) reveal(x - 1, y)
        if (x - 1 >= 0 && y + 1 < tilesY && grid[x - 1][y + 1].canReveal()) reveal(x - 1, y + 1)
        if (y - 1 >= 0 && grid[x][y - 1].canReveal()) reveal(x, y - 1)
        if (y + 1 < tilesY && grid[x][y + 1].canReveal()) reveal(x, y + 1)
        if (x + 1 < tilesX && y - 1 >= 0 && grid[x + 1][y - 1].canReveal()) reveal(x + 1, y - 1)
        if (x + 1 < tilesX && grid[x + 1][y].canReveal()) reveal(x + 1, y)
        if (x + 1 < tilesX && y + 1 < tilesY && grid[x + 1][y + 1].canReveal() ) reveal(x + 1, y + 1)
    }

    private inner class Tile(var posx : Int, var posy : Int, var hasBomb : Boolean) : StackPane() {
        var hasFlag = false
        var isOpen = false
        var isFirstClick = false
        var flagImage= ImageView(flag)
        var image = ImageView(zero)
        var space = ImageView(begin)
        var countedBombs = 0

        init {
            flagImage.isVisible=false
        }

        fun initImage(){
            children.setAll(image,space,flagImage)
        }

        fun clicked() : Boolean{
            if(hasFlag)
                return false
            if(hasBomb)
                return true
            isOpen=true
            space.isVisible=false
            return false
        }

        fun showYourself() {
            if(!isOpen){
                if(!hasFlag)
                isOpen=true
                space.isVisible=false
            }
        }

        fun flagged() = if(hasFlag) {
            hasFlag=false
            flagImage.isVisible=false
            setBombs(true)
        }
        else {
            hasFlag=true
            flagImage.isVisible=true
            setBombs(false)
        }

        fun canReveal() = !hasBomb&&!hasFlag&&!isOpen
    }

    private object InfoHeader : HBox(){
        var image = StackPane()
        var flagBombCounter = StackPane()
        var bombs=50
        var rectangle : Rectangle
        var label : Label

        init {
            background = Background(BackgroundFill(Color.color(96.0/255, 96.0/255, 96.0/255), null, null))
            resize(WIDTH.toDouble(), 50.0)
            rectangle = Rectangle(90.0,50.0,Color.BLACK)
            label = Label(bombs.toString())
            label.textFill=Color.RED
            label.font= Font(30.0)
            flagBombCounter= StackPane()
            flagBombCounter.children.setAll(rectangle,label)
            alignment=Pos.CENTER
            image.children.setAll(ImageView(sadSmiley), ImageView(smiley))
            children.setAll(image,flagBombCounter)
        }

        fun gameOver(){
            image.children[1].isVisible=false
            children[0]=image
        }

        fun setBombs(posOrMin : Boolean){
            if(posOrMin)
                bombs++
            else
                bombs--
            label.text= bombs.toString()
            flagBombCounter.children[1] = label
            children[1]=flagBombCounter
        }

        fun reset(){
            bombs=50
            label.text= bombs.toString()
            flagBombCounter.children[1] = label
            image.children[1].isVisible=true
            children.setAll(image,flagBombCounter)
        }

        fun youWon() {
            label.text="You won!"
            label.font=Font(20.0)
            flagBombCounter.children[1] = label
        }
    }
}
