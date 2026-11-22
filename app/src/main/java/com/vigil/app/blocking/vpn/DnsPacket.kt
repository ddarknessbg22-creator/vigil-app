package com.vigil.app.blocking.vpn

/**
 * Minimal DNS wire-format helper: enough to read the queried name out of a
 * standard query and to build a NXDOMAIN answer. No external dependency.
 */
object DnsPacket {

    data class Query(val host: String, val id: Int)

    /** Returns null for anything that is not a plain DNS question (UDP 53). */
    fun parseQuery(packet: ByteArray): Query? {
        if (packet.size < 12 + 5) return null
        val destinationPort = ((packet[22].toInt() and 0xFF) shl 8) or (packet[23].toInt() and 0xFF)
        if (destinationPort != 53) return null

        val flags = ((packet[2].toInt() and 0xFF) shl 8) or (packet[3].toInt() and 0xFF)
        if (flags and 0x8000 != 0) return null       // already a response
        val qdCount = ((packet[4].toInt() and 0xFF) shl 8) or (packet[5].toInt() and 0xFF)
        if (qdCount < 1) return null

        val id = ((packet[0].toInt() and 0xFF) shl 8) or (packet[1].toInt() and 0xFF)

        // UDP header is 8 bytes, DNS payload starts at 8 (tun frames have no IP header here).
        val dnsStart = 8
        val host = readName(packet, dnsStart) ?: return null
        if (host.isEmpty()) return null
        return Query(host, id)
    }

    private fun readName(packet: ByteArray, start: Int): String? {
        val sb = StringBuilder()
        var index = start
        var guard = 0
        while (index < packet.size && guard < 128) {
            val len = packet[index].toInt() and 0xFF
            if (len == 0) return sb.toString().trimEnd('.')
            if (len and 0xC0 != 0) return null // compression pointer in a question
            if (index + 1 + len > packet.size) return null
            if (sb.isNotEmpty()) sb.append('.')
            for (i in 1..len) {
                sb.append((packet[index + i].toInt() and 0xFF).toChar())
            }
            index += len + 1
            guard++
        }
        return null
    }

    /** Builds a response mirroring the question with RCODE=3 (NXDOMAIN). */
    fun nxDomain(queryPacket: ByteArray): ByteArray {
        val questionStart = 8
        var questionEnd = questionStart
        // walk the QNAME to its terminator
        while (questionEnd < queryPacket.size) {
            val len = queryPacket[questionEnd].toInt() and 0xFF
            if (len == 0) {
                questionEnd += 1
                break
            }
            if (len and 0xC0 != 0) break
            questionEnd += len + 1
        }
        val questionLength = (questionEnd - questionStart) + 4
        val response = ByteArray(questionStart + questionLength)
        System.arraycopy(queryPacket, 0, response, 0, minOf(response.size, queryPacket.size))

        // flags: response, recursion desired/available, NXDOMAIN
        response[2] = (0x81).toByte()
        response[3] = (0x83).toByte()
        response[4] = 0; response[5] = 1          // QDCOUNT = 1
        response[6] = 0; response[7] = 0          // ANCOUNT
        response[8] = 0; response[9] = 0          // NSCOUNT
        response[10] = 0; response[11] = 0        // ARCOUNT
        return response
    }
}
