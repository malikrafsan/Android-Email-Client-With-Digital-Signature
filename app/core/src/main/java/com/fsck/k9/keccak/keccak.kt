package com.fsck.k9.keccak

import java.math.BigInteger
import kotlin.math.min

object Keccak {
    private val BIT_65 = BigInteger.ONE shl (64)
    private val MAX_VAL_64 = BIT_65 - BigInteger.ONE

    fun digest(value: ByteArray): ByteArray {
        val rate = 136
        val outputLength = 32 // bytes
        val d = 0x01

        val uState = IntArray(200)
        val uMessage = toUInt(value)

        var blockSize = 0
        var inputOffset = 0

        // Absorb phase
        while (inputOffset < uMessage.size) {
            blockSize = min(rate, uMessage.size - inputOffset)
            for (i in 0 until blockSize) {
                uState[i] = uState[i] xor uMessage[i + inputOffset]
            }

            inputOffset += blockSize

            if (blockSize == rate) {
                keccakF(uState)
                blockSize = 0
            }
        }

        // Padding
        uState[blockSize] = uState[blockSize] xor d
        uState[rate - 1] = uState[rate - 1] xor 0x80
        keccakF(uState)

        // Squeeze phase
        val result = mutableListOf<Byte>()
        var tempOutputLength = outputLength
        while (tempOutputLength > 0) {
            blockSize = min(tempOutputLength, rate)
            for (i in 0 until blockSize) {
                result.add(uState[i].toByte().toInt().toByte())
            }

            tempOutputLength -= blockSize
            if (tempOutputLength > 0) {
                keccakF(uState)
            }
        }

//        println("Results: " + result.toByteArray().toHex())
        return result.toByteArray()
    }

    private fun keccakF(uState: IntArray) {
        val lState = Array(5) { Array(5) { BigInteger.ZERO } }

        for (i in 0..4) {
            for (j in 0..4) {
                val data = IntArray(8)
                val index = 8 * (i + 5 * j)
                uState.copyInto(data, 0, index, index + data.size)
                lState[i][j] = littleEndianToBase64(data)
            }
        }
        roundB(lState)

        uState.fill(0)
        for (i in 0..4) {
            for (j in 0..4) {
                val data = base64ToLittleEndian(lState[i][j])
                data.copyInto(uState, 8 * (i + 5 * j))
            }
        }
    }

    private fun roundB(state: Array<Array<BigInteger>>) {
        var lfsr = 1
        for (round in 0..23) {
            val c = arrayOfNulls<BigInteger>(5)
            val d = arrayOfNulls<BigInteger>(5)

            // Delta step
            // C[x] = A[x,0] xor A[x,1] xor A[x,2] xor A[x,3] xor A[x,4], for x in 0…4
            for (i in 0..4) {
                c[i] = state[i][0].xor(state[i][1]).xor(state[i][2]).xor(state[i][3]).xor(state[i][4])
            }

            // D[x] = C[x-1] xor rot(C[x+1],1), for x in 0…4
            for (i in 0..4) {
                d[i] = c[(i + 4) % 5]!!.xor(c[(i + 1) % 5]!!.rotateLeft(1))
            }

            // A[x,y] = A[x,y] xor D[x], for (x,y) in (0…4,0…4)
            for (i in 0..4) {
                for (j in 0..4) {
                    state[i][j] = state[i][j].xor(d[i]!!)
                }
            }

            // Rho and Pi steps
            var x = 1
            var y = 0
            var current = state[x][y]
            for (i in 0..23) {
                val tX = x
                x = y
                y = (2 * tX + 3 * y) % 5

                val shiftValue = current
                current = state[x][y]

                state[x][y] = shiftValue.rotateLeftSafely((i + 1) * (i + 2) / 2)
            }

            // Chi step
            // A[x,y] = B[x,y] xor ((not B[x+1,y]) and B[x+2,y]),  for (x,y) in (0…4,0…4)
            for (j in 0..4) {
                val temp = arrayOfNulls<BigInteger>(5)
                // temp[x] = B[x,y]
                for (i in 0..4) {
                    temp[i] = state[i][j]
                }

                for (i in 0..4) {
                    val invertVal = temp[(i + 1) % 5]!!.xor(MAX_VAL_64)
                    state[i][j] = temp[i]!!.xor(invertVal.and(temp[(i + 2) % 5]!!))
                }
            }

            // Iota step
            for (i in 0..6) {
                lfsr = (lfsr shl 1 xor (lfsr shr 7) * 0x71) % 256
                val bitPosition = (1 shl i) - 1 //2^i - 1
                if (lfsr and 2 != 0) {
                    state[0][0] = state[0][0].xor(BigInteger.ONE shl bitPosition)
                }
            }
        }
    }

    private fun IntArray.fill(value: Int) {
        for (i in indices) {
            this[i] = value
        }
    }

    private fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

    private fun toUInt(data: ByteArray) = IntArray(data.size) {
        data[it].toInt() and 0xFF
    }

    private fun littleEndianToBase64(data: IntArray): BigInteger {
        val value = data.map { it.toString(16) }
            .map { if (it.length == 2) it else "0$it" }
            .reversed()
            .joinToString("")
        return BigInteger(value, 16)
    }

    private fun base64ToLittleEndian(uLong: BigInteger): IntArray {
        val asHex = uLong.toString(16)
        val asHexPadded = "0".repeat((8 * 2) - asHex.length) + asHex
        return IntArray(8) {
            ((7 - it) * 2).let { pos ->
                asHexPadded.substring(pos, pos + 2).toInt(16)
            }
        }
    }

    private fun BigInteger.rotateLeftSafely(rotate: Int) = rotateLeft(rotate % 64)

    private fun BigInteger.rotateLeft(rotate: Int) = (this shr (64 - rotate)).add(this shl rotate).mod(BIT_65)
}
