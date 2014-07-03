/*
 * Copyright (c) 2013 LDBC
 * Linked Data Benchmark Council (http://ldbc.eu)
 *
 * This file is part of ldbc_socialnet_dbgen.
 *
 * ldbc_socialnet_dbgen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ldbc_socialnet_dbgen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ldbc_socialnet_dbgen.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2011 OpenLink Software <bdsmt@openlinksw.com>
 * All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation;  only Version 2 of the License dated
 * June 1991.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package ldbc.socialnet.dbgen.util;


import org.apache.hadoop.io.WritableComparator;
import ldbc.socialnet.dbgen.util.MapReduceKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.ArrayList;
import java.util.Iterator;

public class MapReduceKeyComparator extends WritableComparator {
    protected MapReduceKeyComparator() {
            super(MapReduceKey.class);
    }

    @Override
    public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2){
        int block1 = readInt(b1, s1);
        int block2 = readInt(b2, s2);
        if( block1 != block2 ) return block1 - block2;
        int key1[] = {readInt(b1,s1+4), readInt(b1,s1+8), readInt(b1,s1+12)};
        int key2[] = {readInt(b2,s2+4), readInt(b2,s2+8), readInt(b2,s2+12)};
        if( key1[0] != key2[0]) return key1[0] - key2[0];
        if( key1[1] != key2[1]) return key1[1] - key2[1];
        if( key1[2] != key2[2]) return key1[2] - key2[2];
        long id1 = readLong(b1,s1+16);
        long id2 = readLong(b2,s2+16);
        return (int)(id1 - id2);
    }
}