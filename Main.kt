package cryptography

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.experimental.xor
import kotlin.text.Charsets.UTF_8

fun main() {
  menu()
}

fun menu(){
    do {
        println("Task (hide, show, exit):")
        val inputOperation = readln()!!.replaceFirst("> ","")
        when(inputOperation){
            "hide"-> {
                println("Input image file:")
                val input = readln()!!.replaceFirst("> " , "")
                println("Output image file:")
                val output = readln()!!.replaceFirst("> ", "")
                println("Message to hide:")
                val messageToHide = readln()!!.replaceFirst("> ", "")
                println("Password:")
                val password = readln()!!.replaceFirst("> ","")
                try {
                    val workingDirectory = System.getProperty("user.dir")
                    val separator = File.separator
                    val inputFIle = ImageIO.read(File(input))
                    val msgbyteArr = messageToHide.encodeToByteArray()
                    val passwordBytrr = password.encodeToByteArray()
                    val encryptMsg = crypt(msgbyteArr, passwordBytrr)
                    val image =  createImage(inputFIle , encryptMsg)
                    val outputFile = File(output)
                    saveImage(image,outputFile )
                    println("Message saved in $output image.")
                }catch (e : Exception){
                    println("The input image is not large enough to hold this message.")
                }
            }
            "show"-> {
                println("Input image file:")
                val input = readln()!!.replaceFirst("> ", "")

                println("Password:")
                val password = readln()!!.replaceFirst("> ","").encodeToByteArray()
                println("Message:")
                val message = show(File(input), password)
                println(message)
            }
            "exit"-> println("Bye!")
            else -> println("Wrong task: $inputOperation")
        }
    }while (inputOperation != "exit")

}


fun createImage (image: BufferedImage, byteArr: ByteArray ) : BufferedImage{
    val newImage: BufferedImage = image
    val bits = byteArr.map { byte -> (0..7).map { byte.toInt() shl it and 0xFF shr 7 } }.flatten().toIntArray()
    bits.withIndex().forEach {
        val x = it.index % image.width
        val y = it.index / image.width
        val modified = image.getRGB(x, y).toUInt() and 0xFFFFFFFEu or it.value.toUInt()
        image.setRGB(x, y, modified.toInt())
    }
    return newImage
}


fun saveImage(image: BufferedImage, imageFile: File) {
    ImageIO.write(image, "png", imageFile)
}

fun show(inputFile: File, password: ByteArray): String {
    val image = ImageIO.read(inputFile)
    val bytes = mutableListOf<Int>()
    for (byte in generateSequence(0) { it + 1 }
        .map {
            val x = it % image.width
            val y = it / image.width
            image.getRGB(x, y) and 1
        }
        .chunked(8)
        .map { it.reduce { byte, i -> byte shl 1 or i } }) {
        bytes.add(byte)
        val lastIndex = bytes.lastIndex
        if (bytes.size >= 3 && bytes[lastIndex] == 3 && bytes[lastIndex - 1] == 0 && bytes[lastIndex - 2] == 0) {
            break
        }
    }
    val encryptMsg  = bytes.dropLast(3).map { it.toByte() }.toByteArray()
    return decryptMsg(encryptMsg , password)
}

fun crypt(msg : ByteArray , password : ByteArray) : ByteArray{
    val cryptList = mutableListOf<Byte>()
    for(index in msg.indices){
        cryptList.add(msg[index] xor password[index % password.size])
    }
    return cryptList.toByteArray() + "003".map { it.toString().toInt().toByte() }
}
fun decryptMsg(encrypt : ByteArray , password: ByteArray) : String{
    val orignMsg = mutableListOf<Byte>()
    for(index in encrypt.indices){
        orignMsg.add(encrypt[index] xor password[index % password.size])
    }
    return  orignMsg.toByteArray().toString(UTF_8)
}