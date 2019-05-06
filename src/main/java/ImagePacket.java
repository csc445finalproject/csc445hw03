import Misc.Constants;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;

public class ImagePacket {

    int imageNum;
    ArrayList<ImageChunk> imageChunks;

    public ImagePacket(int imageNum) {
        this.imageNum = imageNum;
        imageChunks = new ArrayList<ImageChunk>();
    }

    public void addChunk(byte [] bytes){
        imageChunks.add(new ImageChunk(bytes));
    }


    public byte [] getImageData(){

        if (isValid()) {
            //sort everything first
            this.sort();
            int arrLength = ((imageChunks.size()-1) * Constants.BUFFER_SIZE) + (imageChunks.get(imageChunks.size()-1).data.length);
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

    public void sort(){
        Collections.sort(imageChunks);
    }

    public boolean isValid(){

        return imageChunks.size() == imageChunks.get(0).numChunks;

    }




    static class ImageChunk implements Comparable {

        /*

         4 bytes(int)  2 bytes(short)  2 bytes(short)     byte[]
            -------------------------------------------------------
           | imageNum |  order         |   numChunks |    data    |
            -------------------------------------------------------

         */


        byte [] data;
        int imageNum;
        short order;
        short numChunks;

        public ImageChunk(byte [] b, int imageNum, short order, short numChunks) {
            this.data = b;
            this.imageNum = imageNum;
            this.order = order;
            this.numChunks = numChunks;
        }


        @Override
        public int compareTo(Object o) {
            return this.imageNum - ((ImageChunk)o).imageNum;
        }


        public byte [] getBytes() {
            ByteBuffer bb = ByteBuffer.allocate(Constants.BUFFER_SIZE + Integer.BYTES + (Short.BYTES*2));
            bb.putInt(imageNum);
            bb.putShort(order);
            bb.putShort(numChunks);
            bb.put(data);
            return bb.array();

        }


    }












}
