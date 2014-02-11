/*-
 * Copyright (C) 2006-2007 Erik Larsson
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catacombae.storage.ps.gpt.types;

import org.catacombae.storage.ps.Partition;
import org.catacombae.util.Util;
import java.io.PrintStream;
import org.catacombae.csjc.StructElements;
import org.catacombae.csjc.structelements.Dictionary;
import org.catacombae.csjc.structelements.EncodedStringField;
import org.catacombae.storage.ps.PartitionType;
import org.catacombae.storage.ps.gpt.GPTPartitionType;

/** This class was generated by CStructToJavaClass. */
public class GPTEntry implements Partition, StructElements {

    /*
     * struct GPTEntry
     * size: 128 bytes
     * description:
     *
     * BP  Size  Type      Identifier           Description
     * ----------------------------------------------------
     * 0   16    byte[16]  partitionTypeGUID
     * 16  16    byte[16]  uniquePartitionGUID
     * 32  8     UInt64    startingLBA
     * 40  8     UInt64    endingLBA
     * 48  8     UInt64    attributeBits
     * 56  72    byte[72]  partitionName
     */

    protected final GUID partitionTypeGUID;
    protected final GUID uniquePartitionGUID;
    protected final byte[] startingLBA = new byte[8];
    protected final byte[] endingLBA = new byte[8];
    protected final byte[] attributeBits = new byte[8];
    protected final byte[] partitionName = new byte[72];

    protected final int blockSize;

    public GPTEntry(byte[] data, int offset, int blockSize) {
        this(blockSize, new GUID(data, offset + 0),
                new GUID(data, offset + 16));
        System.arraycopy(data, offset + 32, startingLBA, 0, 8);
        System.arraycopy(data, offset + 40, endingLBA, 0, 8);
        System.arraycopy(data, offset + 48, attributeBits, 0, 8);
        System.arraycopy(data, offset + 56, partitionName, 0, 72);
    }

    /**
     * Used by its evil mutable twin.
     * @param blockSize
     */
    protected GPTEntry(int blockSize, GUID partitionTypeGUID,
            GUID uniquePartitionGUID)
    {
        this.blockSize = blockSize;
        this.partitionTypeGUID = partitionTypeGUID;
        this.uniquePartitionGUID = uniquePartitionGUID;
    }

    public GPTEntry(GPTEntry source) {
        this(source.blockSize, new GUID(source.partitionTypeGUID),
                new GUID(source.uniquePartitionGUID));
        copyFields(source);
    }

    protected void copyFields(GPTEntry source) {
        partitionTypeGUID.copyFields(source.partitionTypeGUID);
        uniquePartitionGUID.copyFields(source.uniquePartitionGUID);
        System.arraycopy(source.startingLBA, 0, startingLBA, 0, startingLBA.length);
        System.arraycopy(source.endingLBA, 0, endingLBA, 0, endingLBA.length);
        System.arraycopy(source.attributeBits, 0, attributeBits, 0, attributeBits.length);
        System.arraycopy(source.partitionName, 0, partitionName, 0, partitionName.length);
    }

    // Defined in Partition
    public long getStartOffset() { return getStartingLBA()*blockSize; }

    public long getLength() {
        /* NOTE: I think this is an appropriate length calculation. If we don't
         * add 1 to getEndingLBA() (which specifies the last sector LBA,
         * inclusive) we are missing one sector. */
        return (getEndingLBA() + 1) * blockSize - getStartOffset();
    }

    public PartitionType getType() { return convertPartitionType(getPartitionTypeGUIDAsEnum()); }

    public static int getSize() { return 128; }

    public GUID getPartitionTypeGUID() { return partitionTypeGUID; }
    public GUID getUniquePartitionGUID() { return uniquePartitionGUID; }
    public long getStartingLBA() { return Util.readLongLE(startingLBA); }
    public long getEndingLBA() { return Util.readLongLE(endingLBA); }
    public long getAttributeBits() { return Util.readLongBE(attributeBits); }
    public byte[] getPartitionName() { return Util.createCopy(partitionName); }

    public GPTPartitionType getPartitionTypeGUIDAsEnum() {
        byte[] guidBytes = partitionTypeGUID.getBytes();
        return GPTPartitionType.getType(Util.readLongBE(guidBytes, 0),
                Util.readLongBE(guidBytes, 8));
    }

    public String getPartitionNameAsString() {
        return getPartitionNameAsString(partitionName);
    }

    public static String getPartitionNameAsString(byte[] partitionName) {
        // Find null terminator
        int stringLength = 0;
        for(int i = 0; i < partitionName.length; i += 2) {
            if(partitionName[i] == 0 && partitionName[i + 1] == 0)
                break;
            else
                stringLength += 2;
        }
        return Util.readString(partitionName, 0, stringLength, "UTF-16LE");
    }

    public boolean isUsed() {
        return getPartitionTypeGUIDAsEnum() != GPTPartitionType.PARTITION_TYPE_UNUSED_ENTRY;
    }

    @Override
    public String toString() {
        return "\"" + getPartitionNameAsString() + "\" (" + getPartitionTypeGUIDAsEnum() + ")";
    }

    public void printFields(PrintStream ps, String prefix) {
        ps.println(prefix + " partitionTypeGUID: " + getPartitionTypeGUID().toString() + " (" + getPartitionTypeGUIDAsEnum() + ")");
        ps.println(prefix + " uniquePartitionGUID: " + getUniquePartitionGUID().toString());
        ps.println(prefix + " startingLBA: " + getStartingLBA());
        ps.println(prefix + " endingLBA: " + getEndingLBA());
        ps.println(prefix + " attributeBits: " + getAttributeBits());
        ps.println(prefix + " partitionName: " + getPartitionNameAsString());
    }

    public void print(PrintStream ps, String prefix) {
        ps.println(prefix + "GPTEntry:");
        printFields(ps, prefix);
    }

    public byte[] getBytes() {
        byte[] result = new byte[128];
        int offset = 0;
        partitionTypeGUID.getBytes(result, offset); offset += GUID.length();
        uniquePartitionGUID.getBytes(result, offset); offset += GUID.length();
        System.arraycopy(startingLBA, 0, result, offset, startingLBA.length); offset += 8;
        System.arraycopy(endingLBA, 0, result, offset, endingLBA.length); offset += 8;
        System.arraycopy(attributeBits, 0, result, offset, attributeBits.length); offset += 8;
        System.arraycopy(partitionName, 0, result, offset, partitionName.length); offset += 72;

        return result;
    }

    @Override
    public boolean equals(Object obj) {
	if(obj instanceof GPTEntry) {
	    GPTEntry gpte = (GPTEntry)obj;
	    return Util.arraysEqual(getBytes(), gpte.getBytes());
	    // Lazy man's method, generating new allocations and work for the GC. But it's convenient.
	}
	else
	    return false;
    }

    // Utility methods

    public static String getGUIDAsString(byte[] guid) {
        String res = "{";
        res += Util.toHexStringLE(Util.readIntBE(guid, 0)) + "-";
        res += Util.toHexStringLE(Util.readShortBE(guid, 4)) + "-";
        res += Util.toHexStringLE(Util.readShortBE(guid, 6)) + "-";
        res += Util.byteArrayToHexString(guid, 8, 2) + "-";
        res += Util.byteArrayToHexString(guid, 10, 6) + "}";
        return res.toUpperCase();
    }

    public static PartitionType convertPartitionType(GPTPartitionType gpt) {
        switch(gpt) {
            case PARTITION_TYPE_APPLE_HFS:
                return PartitionType.APPLE_HFS_CONTAINER;
            case PARTITION_TYPE_PRIMARY_PARTITION:
                return PartitionType.NT_OS2_IFS;
            default:
                return PartitionType.UNKNOWN;
        }
    }

    public Dictionary getStructElements() {
        DictionaryBuilder db = new DictionaryBuilder(GPTEntry.class.getSimpleName());

        db.add("partitionTypeGUID", partitionTypeGUID.getStructElements());
        db.add("uniquePartitionGUID", uniquePartitionGUID.getStructElements());
        db.addUIntLE("startingLBA", startingLBA);
        db.addUIntLE("endingLBA", endingLBA);
        db.add("partitionName", new EncodedStringField(partitionName, "UTF-16LE"));

        return db.getResult();
    }
}
