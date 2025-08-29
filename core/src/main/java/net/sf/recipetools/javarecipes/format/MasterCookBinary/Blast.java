/**
 * 
 */
package net.sf.recipetools.javarecipes.format.MasterCookBinary;

import java.util.Arrays;

import net.sf.recipetools.javarecipes.model.RecipeFoxException;

/** blast.h -- interface for blast.c
  Copyright (C) 2003 Mark Adler
  version 1.1, 16 Feb 2003

  This software is provided 'as-is', without any express or implied
  warranty.  In no event will the author be held liable for any damages
  arising from the use of this software.

  Permission is granted to anyone to use this software for any purpose,
  including commercial applications, and to alter it and redistribute it
  freely, subject to the following restrictions:

  1. The origin of this software must not be misrepresented; you must not
     claim that you wrote the original software. If you use this software
     in a product, an acknowledgment in the product documentation would be
     appreciated but is not required.
  2. Altered source versions must be plainly marked as such, and must not be
     misrepresented as being the original software.
  3. This notice may not be removed or altered from any source distribution.

  Mark Adler    madler@alumni.caltech.edu
 * 
 * Converted from blast.c
 * Copyright (C) 2003 Mark Adler
 * For conditions of distribution and use, see copyright notice in blast.h
 * version 1.1, 16 Feb 2003
 *
 * blast.c decompresses data compressed by the PKWare Compression Library.
 * This function provides functionality similar to the explode() function of
 * the PKWare library, hence the name "blast".
 *
 * blast() decompresses the PKWare Data Compression Library (DCL) compressed
 * format.  It provides the same functionality as the explode() function in
 * that library.  (Note: PKWare overused the "implode" verb, and the format
 * used by their library implode() function is completely different and
 * incompatible with the implode compression method supported by PKZIP.)
 *
 * This decompressor is based on the excellent format description provided by
 * Ben Rudiak-Gould in comp.compression on August 13, 2001.  Interestingly, the
 * example Ben provided in the post is incorrect.  The distance 110001 should
 * instead be 111000.  When corrected, the example byte stream becomes:
 *
 *    00 04 82 24 25 8f 80 7f
 *
 * which decompresses to "AIAIAIAIAIAIA" (without the quotes).
 * 
 * @author Frank
 *
 */
public class Blast {

	/** maximum code length */
	public static final int MAXBITS = 13;
	/** maximum window size */
	public static final int MAXWIN = 4096;             

    static short[] litcnt = new short[MAXBITS+1];
    static short[] litsym = new short[256];        /* litcode memory */
    
    static short[] lencnt = new short[MAXBITS+1];
    static short[] lensym = new short[16];         /* lencode memory */
    
    static short[] distcnt = new short[MAXBITS+1];
    static short[] distsym = new short[64];       /* distcode memory */
    
    static Huffman litcode = new Huffman(litcnt, litsym);   /* length code */
    static Huffman lencode = new Huffman(lencnt, lensym);   /* length code */
    static Huffman distcode = new Huffman(distcnt, distsym);/* distance code */
        /* bit lengths of literal codes */
    static short[] litlen = new short[]{
        11, 124, 8, 7, 28, 7, 188, 13, 76, 4, 10, 8, 12, 10, 12, 10, 8, 23, 8,
        9, 7, 6, 7, 8, 7, 6, 55, 8, 23, 24, 12, 11, 7, 9, 11, 12, 6, 7, 22, 5,
        7, 24, 6, 11, 9, 6, 7, 22, 7, 11, 38, 7, 9, 8, 25, 11, 8, 11, 9, 12,
        8, 12, 5, 38, 5, 38, 5, 11, 7, 5, 6, 21, 6, 10, 53, 8, 7, 24, 10, 27,
        44, 253, 253, 253, 252, 252, 252, 13, 12, 45, 12, 45, 12, 61, 12, 45,
        44, 173};
        /* bit lengths of length codes 0..15 */
    static short[] lenlen = new short[] {2, 35, 36, 53, 38, 23};
        /* bit lengths of distance codes 0..63 */
    static short[] distlen = new short[] {2, 20, 53, 230, 247, 151, 248};
    static short[] base = new short[] {     /* base for length codes */
        3, 2, 4, 5, 6, 7, 8, 9, 10, 12, 16, 24, 40, 72, 136, 264};
    static byte[] extra = new byte[] {     /* extra bits for length codes */
        0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8};
	
	
    static {
        /* set up decoding tables (once--might not be thread-safe) */
    	litcode.construct(litlen, litlen.length);
    	lencode.construct(lenlen, lenlen.length);
    	distcode.construct(distlen, distlen.length);
    }
	
	
	
    /* input state */
	byte[]	in;
	int		inIndex;
    int		left;              /* available input at in: TODO: = length-inIndex */
    int bitbuf;                 /* bit buffer */
    int bitcnt;                 /* number of bits in bit buffer */

    /* output state */
    byte[]	result;
    int resultIndex = 0;
    int		next;              /* index of next write location in out[] */
    boolean first;                  /* true to check distances (for first 4K) */
    byte[] out = new byte[MAXWIN];  /* output buffer and sliding window */
	
    
    /*
     * Return need bits from the input stream.  This always leaves less than
     * eight bits in the buffer.  bits() works properly for need == 0.
     *
     * Format notes:
     *
     * - Bits are stored in bytes from the least significant bit to the most
     *   significant bit.  Therefore bits are dropped from the bottom of the bit
     *   buffer, using shift right, and new bytes are appended to the top of the
     *   bit buffer, using shift left.
     */
    private int bits(int need)
    {
        int val = 0;            /* bit accumulator */

        /* load at least need bits into val */
        val = bitbuf;
        while (bitcnt < need) {
            if (left == 0) {
            	throw new RecipeFoxException("Not enough input bytes");
            }
            val |= (getFromInBuffer()) << bitcnt;          /* load eight bits */
            left--;
            bitcnt += 8;
        }

        /* drop need bits and update buffer, always zero to seven bits left */
        bitbuf = val >> need;
        bitcnt -= need;

        /* return need bits, zeroing the bits above that */
        return val & ((1 << need) - 1);
//        System.out.println(""+need+", "+returnValue+" bitbuf"+bitbuf
//        		+" bitcnt="+bitcnt+" next="+next+" left="+left);
    }

    /**
     * @return An unsigned int from the input byte[]
     */
    int getFromInBuffer() {
    	int r = in[inIndex++];
    	if (r<0) {
    		r += 256;
    	}
    	return r;
    }
    
    /*
     * Decode a code from the stream s using huffman table h.  Return the symbol or
     * a negative value if there is an error.  If all of the lengths are zero, i.e.
     * an empty code, or if the code is incomplete and an invalid code is received,
     * then -9 is returned after reading MAXBITS bits.
     *
     * Format notes:
     *
     * - The codes as stored in the compressed data are bit-reversed relative to
     *   a simple integer ordering of codes of the same lengths.  Hence below the
     *   bits are pulled from the compressed data one at a time and used to
     *   build the code value reversed from what is in the stream in order to
     *   permit simple integer comparisons for decoding.
     *
     * - The first code for the shortest length is all ones.  Subsequent codes of
     *   the same length are simply integer decrements of the previous code.  When
     *   moving up a length, a one bit is appended to the code.  For a complete
     *   code, the last code of the longest length will be all zeros.  To support
     *   this ordering, the bits pulled during decoding are inverted to apply the
     *   more "natural" ordering starting with all zeros and incrementing.
     */
    int decode(Huffman h)
    {
        int len;            /* current number of bits in code */
        int code;           /* len bits being decoded */
        int start;          /* first code of length len */
        int count;          /* number of codes of length len */
        int index;          /* index of first code of length len in symbol table */
        int lbitbuf;         /* bits from stream */
        int lleft;           /* bits left in next or left to process */
        int nextIndex;        /* next number of codes */

        lbitbuf = bitbuf;
        lleft = bitcnt;
        code = start = index = 0;
        len = 1;
        nextIndex = 1;
        while (true) {
            while (lleft-- > 0) {
                code |= (lbitbuf & 1) ^ 1;   /* invert code */
                lbitbuf >>= 1;
                count = h.count[nextIndex++];
                if (code < start + count) { /* if length len, return symbol */
                    bitbuf = lbitbuf;
                    bitcnt = (bitcnt - len) & 7;
                    return h.symbol[index + (code - start)];
                }
                index += count;             /* else update for next length */
                start += count;
                start <<= 1;
                code <<= 1;
                len++;
            }
            lleft = (MAXBITS+1) - len;
            if (lleft == 0) {
                break;
            }
            if (left == 0) {
            	throw new RecipeFoxException("Not enough input bytes");
            }
            lbitbuf = getFromInBuffer();
            left--;
            if (lleft > 8) {
                lleft = 8;
            }
        }
        throw new RecipeFoxException("Data error: ran out of codes");                          /* ran out of codes */
    }

    
    /*
     * Decode PKWare Compression Library stream.
     *
     * Format notes:
     *
     * - First byte is 0 if literals are uncoded or 1 if they are coded.  Second
     *   byte is 4, 5, or 6 for the number of extra bits in the distance code.
     *   This is the base-2 logarithm of the dictionary size minus six.
     *
     * - Compressed data is a combination of literals and length/distance pairs
     *   terminated by an end code.  Literals are either Huffman coded or
     *   uncoded bytes.  A length/distance pair is a coded length followed by a
     *   coded distance to represent a string that occurs earlier in the
     *   uncompressed data that occurs again at the current location.
     *
     * - A bit preceding a literal or length/distance pair indicates which comes
     *   next, 0 for literals, 1 for length/distance.
     *
     * - If literals are uncoded, then the next eight bits are the literal, in the
     *   normal bit order in th stream, i.e. no bit-reversal is needed. Similarly,
     *   no bit reversal is needed for either the length extra bits or the distance
     *   extra bits.
     *
     * - Literal bytes are simply written to the output.  A length/distance pair is
     *   an instruction to copy previously uncompressed bytes to the output.  The
     *   copy is from distance bytes back in the output stream, copying for length
     *   bytes.
     *
     * - Distances pointing before the beginning of the output data are not
     *   permitted.
     *
     * - Overlapped copies, where the length is greater than the distance, are
     *   allowed and common.  For example, a distance of one and a length of 518
     *   simply copies the last byte 518 times.  A distance of four and a length of
     *   twelve copies the last four bytes three times.  A simple forward copy
     *   ignoring whether the length is greater than the distance or not implements
     *   this correctly.
     */
    int decomp()
    {
        int lit;            /* true if literals are coded */
        int dict;           /* log2(dictionary size) - 6 */
        int symbol;         /* decoded symbol, extra bits for distance */
        int len;            /* length for copy */
        int dist;           /* distance for copy */
        int copy;           /* copy counter */
        int fromIndex;    /* copy pointers */
        int toIndex;


        /* read header */
        lit = bits(8);
        if (lit > 1) {
            throw new RecipeFoxException ("Invalid header: byte[0] should be 0 or 1, was "+lit);
        }
        dict = bits(8);
        if (dict < 4 || dict > 6) {
            throw new RecipeFoxException ("Invalid header: dict=byte[1] should be 4-6.");
        }

        /* decode literals and length/distance pairs */
        do {
            if (bits(1)!=0) {
                /* get length */
                symbol = decode(lencode);
                len = base[symbol] + bits(extra[symbol]);
                if (len == 519) {
                    break;              /* end code */
                }

                /* get distance */
                symbol = (len == 2) ? 2 : dict;
                dist = decode(distcode) << symbol;
                dist += bits(symbol);
                dist++;
                if (first && dist > next) {
                    throw new RecipeFoxException("Error in compression: distance too far back");              /* distance too far back */
                }

                /* copy length bytes from distance bytes back */
                do {
                    toIndex = next;
                    fromIndex = toIndex - dist;
                    copy = MAXWIN;
                    if (next < dist) {
                        fromIndex += copy;
                        copy = dist;
                    }
                    copy -= next;
                    if (copy > len) {
                        copy = len;
                    }
                    len -= copy;
                    next += copy;
                    do {
                        out[toIndex++] = out[fromIndex++];
                    } while (--copy != 0);
                    if (next == MAXWIN) {
                    	// copy out window to result array.
                        copyOutToResult();
                        next = 0;
                        first = false;
                    }
                } while (len != 0);
            }
            else {
                /* get literal and write it */
                symbol = lit!=0 ? decode(litcode) : bits(8);
                out[next++] = (byte) symbol;
                if (next == MAXWIN) {
                    copyOutToResult();
                    next = 0;
                    first = false;
                }
            }
        } while (true);
        return 0;
    }

    private void copyOutToResult() {
    	if (resultIndex + next > result.length) {
    		// Expand result array.
    		byte[] newResult = Arrays.copyOf(result, Math.max(result.length*2, (resultIndex + next)*2));
    		result = newResult;
    	}
    	for (int i=0;i<next; i++) {
    		result[resultIndex++] = out[i];
    	}
    	next = 0;
	}

	public byte[] blast(byte[] input) {
    	

        /* initialize input state */
    	in = input;
    	inIndex = 0;
        left = input.length;
        bitbuf = 0;
        bitcnt = 0;

        /* initialize output state */
    	result = new byte[input.length * 10];
        resultIndex = 0;
        next = 0;
        first = true;
        
        /* return if bits() or decode() tries to read past available input */
        decomp();               /* decompress */

        /* write any leftover output and update the error code if needed */
        copyOutToResult();

        return Arrays.copyOf(result, resultIndex);
    }
	
}


/*
 * Huffman code decoding tables.  count[1..MAXBITS] is the number of symbols of
 * each length, which for a canonical code are stepped through in order.
 * symbol[] are the symbol values in canonical order, where the number of
 * entries is the sum of the counts in count[].  The decoding process can be
 * seen in the function decode() below.
 */
class Huffman {
    public short[] count;       /* number of symbols of each length */
    public short[] symbol;      /* canonically ordered symbols */
    public Huffman(short[] count, short[] symbol) {
    	this.count = count;
    	this.symbol = symbol;
    }

    /*
     * Given a list of repeated code lengths rep[0..n-1], where each byte is a
     * count (high four bits + 1) and a code length (low four bits), generate the
     * list of code lengths.  This compaction reduces the size of the object code.
     * Then given the list of code lengths length[0..n-1] representing a canonical
     * Huffman code for n symbols, construct the tables required to decode those
     * codes.  Those tables are the number of codes of each length, and the symbols
     * sorted by length, retaining their original order within each length.  The
     * return value is zero for a complete code set, negative for an over-
     * subscribed code set, and positive for an incomplete code set.  The tables
     * can be used if the return value is zero or positive, but they cannot be used
     * if the return value is negative.  If the return value is zero, it is not
     * possible for decode() using that table to return an error--any stream of
     * enough bits will resolve to a symbol.  If the return value is positive, then
     * it is possible for decode() using that table to return an error for received
     * codes past the end of the incomplete lengths.
     */
    public int construct(short[] rep, int n)
    {
    	int repIndex=0;
        int symbolIndex;         /* current symbol when stepping through length[] */
        int len;            /* current length when stepping through h->count[] */
        int left;           /* number of possible codes left of current length */
        short[] offs = new short[Blast.MAXBITS+1];      /* offsets in symbol table for each length */
        short[] length = new short[256];  /* code lengths */

        /* convert compact repeat counts into symbol bit length list */
        symbolIndex = 0;
        do {
            len = rep[repIndex++];
            left = (len >> 4) + 1;
            len &= 15;
            do {
                length[symbolIndex++] = (short) len;
            } while (--left > 0);
        } while (--n > 0);
        n = symbolIndex;

        /* count number of codes of each length */
        for (len = 0; len <= Blast.MAXBITS; len++) {
            count[len] = 0;
        }
        for (symbolIndex = 0; symbolIndex < n; symbolIndex++) {
            (count[length[symbolIndex]])++;   /* assumes lengths are within bounds */
        }
        if (count[0] == n) {              /* no codes! */
            return 0;                       /* complete, but decode() will fail */
        }

        /* check for an over-subscribed or incomplete set of lengths */
        left = 1;                           /* one possible code of zero length */
        for (len = 1; len <= Blast.MAXBITS; len++) {
            left <<= 1;                     /* one more bit, double codes left */
            left -= count[len];          /* deduct count from possible codes */
            if (left < 0) {
            	return left;			    /* over-subscribed--return negative */
            }
        }                                   /* left > 0 means incomplete */

        /* generate offsets into symbol table for each length for sorting */
        offs[1] = 0;
        for (len = 1; len < Blast.MAXBITS; len++) {
            offs[len + 1] = (short) (offs[len] + count[len]);
        }

        /*
         * put symbols in table sorted by length, by symbol order within each
         * length
         */
        for (symbolIndex = 0; symbolIndex < n; symbolIndex++) {
        	if (length[symbolIndex] != 0) {
                this.symbol[offs[length[symbolIndex]]++] = (short) symbolIndex;
        	}
        }

        /* return zero for complete set, positive for incomplete set */
        return left;
    }
};

