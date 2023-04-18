package com.fsck.k9.SAES2
import java.math.BigInteger
import java.util.Base64

class SAES2 (rawMasterKey: String) {
    private val blockSize = 16
    private val keySize = 16
    private var masterKey: BigInteger
    private var roundKeys : ArrayList<ArrayList<Int>> = ArrayList()
    private val sBox: IntArray = intArrayOf(
        0x63, 0x7C, 0x77, 0x7B, 0xF2, 0x6B, 0x6F, 0xC5, 0x30, 0x01, 0x67, 0x2B, 0xFE, 0xD7, 0xAB, 0x76,
        0xCA, 0x82, 0xC9, 0x7D, 0xFA, 0x59, 0x47, 0xF0, 0xAD, 0xD4, 0xA2, 0xAF, 0x9C, 0xA4, 0x72, 0xC0,
        0xB7, 0xFD, 0x93, 0x26, 0x36, 0x3F, 0xF7, 0xCC, 0x34, 0xA5, 0xE5, 0xF1, 0x71, 0xD8, 0x31, 0x15,
        0x04, 0xC7, 0x23, 0xC3, 0x18, 0x96, 0x05, 0x9A, 0x07, 0x12, 0x80, 0xE2, 0xEB, 0x27, 0xB2, 0x75,
        0x09, 0x83, 0x2C, 0x1A, 0x1B, 0x6E, 0x5A, 0xA0, 0x52, 0x3B, 0xD6, 0xB3, 0x29, 0xE3, 0x2F, 0x84,
        0x53, 0xD1, 0x00, 0xED, 0x20, 0xFC, 0xB1, 0x5B, 0x6A, 0xCB, 0xBE, 0x39, 0x4A, 0x4C, 0x58, 0xCF,
        0xD0, 0xEF, 0xAA, 0xFB, 0x43, 0x4D, 0x33, 0x85, 0x45, 0xF9, 0x02, 0x7F, 0x50, 0x3C, 0x9F, 0xA8,
        0x51, 0xA3, 0x40, 0x8F, 0x92, 0x9D, 0x38, 0xF5, 0xBC, 0xB6, 0xDA, 0x21, 0x10, 0xFF, 0xF3, 0xD2,
        0xCD, 0x0C, 0x13, 0xEC, 0x5F, 0x97, 0x44, 0x17, 0xC4, 0xA7, 0x7E, 0x3D, 0x64, 0x5D, 0x19, 0x73,
        0x60, 0x81, 0x4F, 0xDC, 0x22, 0x2A, 0x90, 0x88, 0x46, 0xEE, 0xB8, 0x14, 0xDE, 0x5E, 0x0B, 0xDB,
        0xE0, 0x32, 0x3A, 0x0A, 0x49, 0x06, 0x24, 0x5C, 0xC2, 0xD3, 0xAC, 0x62, 0x91, 0x95, 0xE4, 0x79,
        0xE7, 0xC8, 0x37, 0x6D, 0x8D, 0xD5, 0x4E, 0xA9, 0x6C, 0x56, 0xF4, 0xEA, 0x65, 0x7A, 0xAE, 0x08,
        0xBA, 0x78, 0x25, 0x2E, 0x1C, 0xA6, 0xB4, 0xC6, 0xE8, 0xDD, 0x74, 0x1F, 0x4B, 0xBD, 0x8B, 0x8A,
        0x70, 0x3E, 0xB5, 0x66, 0x48, 0x03, 0xF6, 0x0E, 0x61, 0x35, 0x57, 0xB9, 0x86, 0xC1, 0x1D, 0x9E,
        0xE1, 0xF8, 0x98, 0x11, 0x69, 0xD9, 0x8E, 0x94, 0x9B, 0x1E, 0x87, 0xE9, 0xCE, 0x55, 0x28, 0xDF,
        0x8C, 0xA1, 0x89, 0x0D, 0xBF, 0xE6, 0x42, 0x68, 0x41, 0x99, 0x2D, 0x0F, 0xB0, 0x54, 0xBB, 0x16,
    )
    private val invSBox: IntArray = intArrayOf(
        0x52, 0x09, 0x6A, 0xD5, 0x30, 0x36, 0xA5, 0x38, 0xBF, 0x40, 0xA3, 0x9E, 0x81, 0xF3, 0xD7, 0xFB,
        0x7C, 0xE3, 0x39, 0x82, 0x9B, 0x2F, 0xFF, 0x87, 0x34, 0x8E, 0x43, 0x44, 0xC4, 0xDE, 0xE9, 0xCB,
        0x54, 0x7B, 0x94, 0x32, 0xA6, 0xC2, 0x23, 0x3D, 0xEE, 0x4C, 0x95, 0x0B, 0x42, 0xFA, 0xC3, 0x4E,
        0x08, 0x2E, 0xA1, 0x66, 0x28, 0xD9, 0x24, 0xB2, 0x76, 0x5B, 0xA2, 0x49, 0x6D, 0x8B, 0xD1, 0x25,
        0x72, 0xF8, 0xF6, 0x64, 0x86, 0x68, 0x98, 0x16, 0xD4, 0xA4, 0x5C, 0xCC, 0x5D, 0x65, 0xB6, 0x92,
        0x6C, 0x70, 0x48, 0x50, 0xFD, 0xED, 0xB9, 0xDA, 0x5E, 0x15, 0x46, 0x57, 0xA7, 0x8D, 0x9D, 0x84,
        0x90, 0xD8, 0xAB, 0x00, 0x8C, 0xBC, 0xD3, 0x0A, 0xF7, 0xE4, 0x58, 0x05, 0xB8, 0xB3, 0x45, 0x06,
        0xD0, 0x2C, 0x1E, 0x8F, 0xCA, 0x3F, 0x0F, 0x02, 0xC1, 0xAF, 0xBD, 0x03, 0x01, 0x13, 0x8A, 0x6B,
        0x3A, 0x91, 0x11, 0x41, 0x4F, 0x67, 0xDC, 0xEA, 0x97, 0xF2, 0xCF, 0xCE, 0xF0, 0xB4, 0xE6, 0x73,
        0x96, 0xAC, 0x74, 0x22, 0xE7, 0xAD, 0x35, 0x85, 0xE2, 0xF9, 0x37, 0xE8, 0x1C, 0x75, 0xDF, 0x6E,
        0x47, 0xF1, 0x1A, 0x71, 0x1D, 0x29, 0xC5, 0x89, 0x6F, 0xB7, 0x62, 0x0E, 0xAA, 0x18, 0xBE, 0x1B,
        0xFC, 0x56, 0x3E, 0x4B, 0xC6, 0xD2, 0x79, 0x20, 0x9A, 0xDB, 0xC0, 0xFE, 0x78, 0xCD, 0x5A, 0xF4,
        0x1F, 0xDD, 0xA8, 0x33, 0x88, 0x07, 0xC7, 0x31, 0xB1, 0x12, 0x10, 0x59, 0x27, 0x80, 0xEC, 0x5F,
        0x60, 0x51, 0x7F, 0xA9, 0x19, 0xB5, 0x4A, 0x0D, 0x2D, 0xE5, 0x7A, 0x9F, 0x93, 0xC9, 0x9C, 0xEF,
        0xA0, 0xE0, 0x3B, 0x4D, 0xAE, 0x2A, 0xF5, 0xB0, 0xC8, 0xEB, 0xBB, 0x3C, 0x83, 0x53, 0x99, 0x61,
        0x17, 0x2B, 0x04, 0x7E, 0xBA, 0x77, 0xD6, 0x26, 0xE1, 0x69, 0x14, 0x63, 0x55, 0x21, 0x0C, 0x7D,
    )
    private val rCon: IntArray = intArrayOf(
        0x00, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40,
        0x80, 0x1B, 0x36, 0x6C, 0xD8, 0xAB, 0x4D, 0x9A,
        0x2F, 0x5E, 0xBC, 0x63, 0xC6, 0x97, 0x35, 0x6A,
        0xD4, 0xB3, 0x7D, 0xFA, 0xEF, 0xC5, 0x91, 0x39,
    )
    private var buffer : Array<BigInteger> = arrayOf()
    private var stateMatrix : ArrayList<ArrayList<Int>> = ArrayList()
    private var playfairKey : ArrayList<ArrayList<Char>> = ArrayList()

    init {
        masterKey = rawMasterKey.convertStringToBigInt()
        changeKey(masterKey)
    }

    fun encrypt(msg: String): String {
        val encodecMsg = msg.toByteArray(Charsets.UTF_8)
        val plainText = padMessage(encodecMsg)

        var blockNums = plainText.size / blockSize
        if (plainText.size % blockSize != 0) {
            blockNums += 1
        }

        buffer = Array(blockNums) { 0.toBigInteger() }
        for (i in 0 until blockNums) {
            buffer[i] = 0.toBigInteger()

            val block = plainText.copyOfRange(i * blockSize, (i + 1) * blockSize)
            buffer[i] = startEncrypt(block.convertToBigInt())

            if (i == 0) {
                buffer[i] = buffer[i] xor masterKey
            } else {
                buffer[i] = buffer[i] xor buffer[i - 1]
            }
        }

        return bufferToText()
    }

    fun decrypt(msg: String): String {
            val decodedMsg = Base64.getDecoder().decode(msg)
            val chunks = splitIntoChunks(decodedMsg)
            buffer = chunks.toTypedArray()
            var result = ""

            for (i in chunks.indices) {
                if (i < buffer.size) {
                    if (i == 0) {
                        buffer[i] = buffer[i] xor masterKey
                    } else {
                        buffer[i] = buffer[i] xor chunks[i - 1]
                    }

                    buffer[i] = startDecrypt(buffer[i])
                    result += buffer[i].toByteArray().toString(Charsets.UTF_8)
                }
            }

            return unPadMessage(result)
        }


    private fun bufferToText(): String {
        var bytesArray = buffer[0].toByteArray()

        for (i in 1 until buffer.size) {
            bytesArray += buffer[i].toByteArray()
        }

        return Base64.getEncoder().encode(bytesArray).toString(Charsets.UTF_8)
    }

    private fun startEncrypt(plainText: BigInteger): BigInteger {
        stateMatrix = textToMatrix(plainText)
        addRoundKey(roundKeys.slice(0 until 4))

        for (i in 1 until 10) {
            roundEncrypt(roundKeys.slice(4*i until 4*(i+1)), i)
        }

        subBytes()
        shiftRows()
        addRoundKey(roundKeys.slice(40 until 44))

        return matrixToText(stateMatrix)
    }

    private fun startDecrypt(cipherText: BigInteger): BigInteger {
        stateMatrix = textToMatrix(cipherText)
        addRoundKey(roundKeys.slice(40 until 44))
        invShiftRows()
        invSubBytes()

        for (i in 9 downTo 1) {
            roundDecrypt(roundKeys.slice(4*i until 4*(i+1)), i)
        }

        addRoundKey(roundKeys.slice(0 until 4))

        return matrixToText(stateMatrix)
    }

    private fun roundEncrypt(keyMatrix: List<ArrayList<Int>>, round: Int) {
        subBytes()
        shiftRows()
        mixColumns()
        addRoundKey(keyMatrix)
        encryptPlayfair(keyMatrix, round)
    }

    private fun roundDecrypt(keyMatrix: List<ArrayList<Int>>, round: Int) {
        decryptPlayfair(keyMatrix, round)
        addRoundKey(keyMatrix)
        invMixColumns()
        invShiftRows()
        invSubBytes()
    }

    private fun addRoundKey(round: List<ArrayList<Int>>) {
        for (i in 0 until 4) {
            for (j in 0 until 4) {
                stateMatrix[i][j] = stateMatrix[i][j] xor round[i][j]
            }
        }
    }

    private fun subBytes() {
        for (i in 0 until 4) {
            for (j in 0 until 4) {
                stateMatrix[i][j] = sBox[stateMatrix[i][j]]
            }
        }
    }

    private fun invSubBytes() {
        for (i in 0 until 4) {
            for (j in 0 until 4) {
                stateMatrix[i][j] = invSBox[stateMatrix[i][j]]
            }
        }
    }

    private fun shiftRows() {
        stateMatrix[0][1] = stateMatrix[1][1].also { stateMatrix[1][1] = stateMatrix[2][1].also { stateMatrix[2][1] = stateMatrix[3][1].also { stateMatrix[3][1] = stateMatrix[0][1] } } }
        stateMatrix[0][2] = stateMatrix[2][2].also { stateMatrix[1][2] = stateMatrix[3][2].also { stateMatrix[2][2] = stateMatrix[0][2].also { stateMatrix[3][2] = stateMatrix[1][2] } } }
        stateMatrix[0][3] = stateMatrix[3][3].also { stateMatrix[1][3] = stateMatrix[0][3].also { stateMatrix[2][3] = stateMatrix[1][3].also { stateMatrix[3][3] = stateMatrix[2][3] } } }
    }

    private fun invShiftRows() {
        stateMatrix[0][1] = stateMatrix[3][1].also { stateMatrix[1][1] = stateMatrix[0][1].also { stateMatrix[2][1] = stateMatrix[1][1].also { stateMatrix[3][1] = stateMatrix[2][1] } } }
        stateMatrix[0][2] = stateMatrix[2][2].also { stateMatrix[1][2] = stateMatrix[3][2].also { stateMatrix[2][2] = stateMatrix[0][2].also { stateMatrix[3][2] = stateMatrix[1][2] } } }
        stateMatrix[0][3] = stateMatrix[1][3].also { stateMatrix[1][3] = stateMatrix[2][3].also { stateMatrix[2][3] = stateMatrix[3][3].also { stateMatrix[3][3] = stateMatrix[0][3] } } }
    }

    private fun mixColumns() {
        var t: Int
        var u: Int
        for (i in 0 until 4) {
            t = stateMatrix[i][0] xor stateMatrix[i][1] xor stateMatrix[i][2] xor stateMatrix[i][3]
            u = stateMatrix[i][0]

            stateMatrix[i][0] = stateMatrix[i][0] xor t xor xTime(stateMatrix[i][0] xor stateMatrix[i][1])
            stateMatrix[i][1] = stateMatrix[i][1] xor t xor xTime(stateMatrix[i][1] xor stateMatrix[i][2])
            stateMatrix[i][2] = stateMatrix[i][2] xor t xor xTime(stateMatrix[i][2] xor stateMatrix[i][3])
            stateMatrix[i][3] = stateMatrix[i][3] xor t xor xTime(stateMatrix[i][3] xor u)
        }
    }

    private fun invMixColumns() {
        var u: Int
        var v: Int
        for (i in 0 until 4) {
            u = xTime(xTime(stateMatrix[i][0] xor stateMatrix[i][2]))
            v = xTime(xTime(stateMatrix[i][1] xor stateMatrix[i][3]))

            stateMatrix[i][0] = stateMatrix[i][0] xor u
            stateMatrix[i][1] = stateMatrix[i][1] xor v
            stateMatrix[i][2] = stateMatrix[i][2] xor u
            stateMatrix[i][3] = stateMatrix[i][3] xor v
        }
        mixColumns()
    }

    private fun encryptPlayfair(keyMatrix: List<ArrayList<Int>>, round: Int) {
        makePlayfairKey(keyMatrix, round)
        for (i in 0 until 4) {
            for (j in 0 until 4) {
                stateMatrix[i][j] = changeByteEncrypt(stateMatrix[i][j])
            }
        }
    }

    private fun decryptPlayfair(keyMatrix: List<ArrayList<Int>>, round: Int) {
        makePlayfairKey(keyMatrix, round)
        for (i in 0 until 4) {
            for (j in 0 until 4) {
                stateMatrix[i][j] = changeByteDecrypt(stateMatrix[i][j])
            }
        }
    }

    private fun makePlayfairKey(keyMatrix: List<ArrayList<Int>>, round: Int) {
        playfairKey = ArrayList()

        for (i in 0 until 4) {
            playfairKey.add(ArrayList())
            for (j in 0 until 4) {
                playfairKey[i].add('0')
            }
        }

        val possibleKey = ArrayList("0123456789abcdef".toList())
        val cRow = keyMatrix[round % 4]

        val isian = arrayListOf<Char>()

        for (i in 0 until 4) {
            val hexa = cRow[i].toString(16)
            val firstByte: Char
            val secondByte: Char

            if (hexa.length == 1) {
                firstByte = '0'
                secondByte = hexa[0]
            } else {
                firstByte = hexa[0]
                secondByte = hexa[1]
            }

            if (firstByte !in isian) {
                isian.add(firstByte)
            }

            if (secondByte !in isian) {
                isian.add(secondByte)
            }
        }

        for (p in possibleKey) {
            if (p !in isian) {
                isian.add(p)
            }
        }

        for (i in 0 until 16) {
            playfairKey[i / 4][i % 4] = isian[i]
        }
    }

    private fun changeByteEncrypt(num: Int): Int {
        val hexa = num.toString(16)
        var c = ""
        val firstByte: Char
        val secondByte: Char

        if (hexa.length == 1) {
            firstByte = '0'
            secondByte = hexa[0]
        } else {
            firstByte = hexa[0]
            secondByte = hexa[1]
        }

        val firstLoc = intArrayOf(0, 0)
        val secondLoc = intArrayOf(0, 0)

        // find character location
        for (i in 0 until 4) {
            for (j in 0 until 4) {
                if (playfairKey[i][j] == firstByte) {
                    firstLoc[0] = i
                    firstLoc[1] = j
                }
                if (playfairKey[i][j] == secondByte) {
                    secondLoc[0] = i
                    secondLoc[1] = j
                }
            }
        }

        // replacing
        if (firstLoc[0] == secondLoc[0]) {
            c += playfairKey[firstLoc[0]][(firstLoc[1] + 1) % 4]
            c += playfairKey[secondLoc[0]][(secondLoc[1] + 1) % 4]
        } else if (firstLoc[1] == secondLoc[1]) {
            c += playfairKey[(firstLoc[0] + 1) % 4][firstLoc[1]]
            c += playfairKey[(secondLoc[0] + 1) % 4][secondLoc[1]]
        } else {
            c += playfairKey[firstLoc[0]][secondLoc[1]]
            c += playfairKey[secondLoc[0]][firstLoc[1]]
        }

        return c.toInt(16)
    }

    private fun changeByteDecrypt(num: Int): Int {
        val hexa = num.toString(16)
        var c = ""
        val firstByte: Char
        val secondByte: Char

        if (hexa.length == 1) {
            firstByte = '0'
            secondByte = hexa[0]
        } else {
            firstByte = hexa[0]
            secondByte = hexa[1]
        }

        val firstLoc = intArrayOf(0, 0)
        val secondLoc = intArrayOf(0, 0)

        // find character location
        for (i in 0 until 4) {
            for (j in 0 until 4) {
                if (playfairKey[i][j] == firstByte) {
                    firstLoc[0] = i
                    firstLoc[1] = j
                }
                if (playfairKey[i][j] == secondByte) {
                    secondLoc[0] = i
                    secondLoc[1] = j
                }
            }
        }

        // replacing
        if (firstLoc[0] == secondLoc[0]) {
            c += playfairKey[firstLoc[0]][(firstLoc[1] + 3) % 4]
            c += playfairKey[secondLoc[0]][(secondLoc[1] + 3) % 4]
        } else if (firstLoc[1] == secondLoc[1]) {
            c += playfairKey[(firstLoc[0] + 3) % 4][firstLoc[1]]
            c += playfairKey[(secondLoc[0] + 3) % 4][secondLoc[1]]
        } else {
            c += playfairKey[firstLoc[0]][secondLoc[1]]
            c += playfairKey[secondLoc[0]][firstLoc[1]]
        }

        return c.toInt(16)
    }

    private fun splitIntoChunks(byteArray: ByteArray): List<BigInteger> {
        val chunks = byteArray.asList().chunked(blockSize)
        return chunks.map { chunk -> chunk.toByteArray().convertToBigInt() }
    }

    private fun xTime(a: Int): Int {
        return if (a and 0x80 != 0) {
            ((a shl 1) xor 0x1B) and 0xFF
        } else {
            a shl 1
        }
    }

    private fun padMessage(plainText: ByteArray): ByteArray {
        val paddingNumber = blockSize - (plainText.size % blockSize)
        val asciiPadding = paddingNumber.toChar()
        var paddString = ""
        for (i in 0 until paddingNumber) {
            paddString += asciiPadding
        }

        return plainText + paddString.toByteArray(Charsets.UTF_8)
    }

    private fun unPadMessage(msg: String): String {
        val paddingNumber = msg[msg.length - 1].code
        return msg.substring(0, msg.length - paddingNumber)
    }

    private fun String.convertStringToBigInt(): BigInteger {
        val byteArray = this.toByteArray(Charsets.UTF_8)
        if (byteArray.size != keySize) {
            throw Error("Key size mismatch, must be 16 bytes")
        }
        return byteArray.toHex().toBigInteger(16)
    }

    private fun ByteArray.convertToBigInt(): BigInteger {
        return this.toHex().toBigInteger(16)
    }

    private fun changeKey(key: BigInteger) {
        roundKeys =  textToMatrix(key)

        for (i in 4..43) {
            roundKeys.add(ArrayList())
            var byte : Int
            if (i % 4 == 0) {
                byte = roundKeys[i - 4][0] xor sBox[roundKeys[i - 1][1]] xor rCon[i / 4]
                roundKeys[i].add(byte)

                for (j in 1..3) {
                    byte = roundKeys[i - 4][j] xor sBox[roundKeys[i - 1][(j + 1) % 4]]
                    roundKeys[i].add(byte)
                }
            } else {
                for (j in 0..3) {
                    byte = roundKeys[i - 4][j] xor roundKeys[i - 1][j]
                    roundKeys[i].add(byte)
                }
            }
        }
    }

    private fun textToMatrix(text: BigInteger): ArrayList<ArrayList<Int>> {
        val matrix : ArrayList<ArrayList<Int>> = ArrayList()
        for (i in 0..15) {
            val byte = ((text shr (8 * (15 - i))) and BigInteger("FF", 16)).toInt()
            if (i % 4 == 0) {
                matrix.add(ArrayList())
            }
            matrix[i / 4].add(byte)
        }
        return matrix
    }

    private fun matrixToText(matrix: ArrayList<ArrayList<Int>>): BigInteger {
        var text = BigInteger("0")
        for (i in 0..15) {
            text = text or (BigInteger(matrix[i / 4][i % 4].toString()) shl (8 * (15 - i)))
        }
        return text
    }

    private fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
}
