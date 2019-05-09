import Misc.Constants;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;

public class ImagePacket {

    int imageNum;
    ArrayList<ImageChunk> imageChunks;


    /**
     *
     * Create a new Image Packet. Each image packet is made up of several image chunks
     *
     * @param imageNum Represents the image number. Image n+1, comes immediately after image n
     */
    public ImagePacket(int imageNum) {
        this.imageNum = imageNum;
        imageChunks = new ArrayList<ImageChunk>();
    }


    /**
     *
     * Adds a new imageChunk into an imagePacket
     *
     * @param bytes Represents a piece of an image
     * @param imageNum the imageNum which is associate with the incoming bytes
     * @param order The order in the ImagePacket where the imageChunk is supposed to be
     * @param numChunks The number of imageChunks we expect to receive in the imagePacket
     */
    public void addChunk(byte [] bytes, int imageNum, short order, short numChunks){
        imageChunks.add(new ImageChunk(bytes, imageNum, order, numChunks));
    }


    /**
     *
     * @return A byte representation of the entire imagePacket. Image chunks within the packet are sorted, and bytes from all imageChunks are put together, and returned
     */

    public byte [] getImageData(){

            if (isValid()) {
                //sort everything first
                this.sort();
                int regulatPacketsLength = ((imageChunks.size()-1) * Constants.IMAGE_CHUNK_SIZE);
                int lastPacketLength = (imageChunks.get(imageChunks.size()-1).data.length);

                int arrLength = regulatPacketsLength + lastPacketLength;
                byte [] imageBytes;
                ByteBuffer buffer = ByteBuffer.allocate(arrLength);

                //copy all bytes from the imageChunks into the total list of bytes
                for (ImageChunk ic: imageChunks) {
                    buffer.put(ic.data);
                }

                imageBytes = buffer.array();
                return imageBytes;


            } else {

                //no image will be returned for this frame
                return null;
            }


    }


    /**
     * Sorts the image chunks within the imagePacket. ImageChunks are sorted by their order field
     */
    public void sort(){
        Collections.sort(imageChunks);
    }


    /**
     *
     * @return true if the imagePacket has all of its imageChunks
     */
    public boolean isValid(){

        return imageChunks.size() == imageChunks.get(0).numChunks;

    }


    /**
     * Class to represent pieces of an image
     */

    static class ImageChunk implements Comparable {

        /**
         * Represents that packet header format
         */

        /*

         4 bytes(int)  2 bytes(short)  2 bytes(short)     byte[] (up to 1024 bytes)
            -------------------------------------------------------
           | imageNum |  order         |   numChunks |    data    |
            -------------------------------------------------------

         */


        byte [] data;
        int imageNum;
        short order;
        short numChunks;


        /**
         *
         * @param b Bytes that represent a section of an image
         * @param imageNum The imageNumber that this chunk belongs to
         * @param order The order in which this imageChunk should be placed. IE, imageChunk 1 comes before 2, 2 before 3, etc.
         * @param numChunks The total number of imageChunks that the imageNum needs to have to be complete.
         */
        public ImageChunk(byte [] b, int imageNum, short order, short numChunks) {
            this.data = b;
            this.imageNum = imageNum;
            this.order = order;
            this.numChunks = numChunks;
        }


        /**
         *
         * @param o The other imageNum we are comparing
         * @return which imageChunk comes first. Sorts values in ascending order
         */
        @Override
        public int compareTo(Object o) {
            return this.imageNum - ((ImageChunk)o).imageNum;
        }


        /**
         *
         * @return a byte representation of an imageChunk following the header description found at the start of the class
         */

        public byte [] getBytes() {
            ByteBuffer bb = ByteBuffer.allocate(data.length + Integer.BYTES + (Short.BYTES*2));
            bb.putInt(imageNum);
            bb.putShort(order);
            bb.putShort(numChunks);
            bb.put(data);
            return bb.array();

        }


    }












}
