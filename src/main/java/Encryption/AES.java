package Encryption;

import Misc.Constants;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AES {

    private SecretKeySpec secretKey;

    private void setKey(byte [] pWord){
        secretKey = new SecretKeySpec(pWord, Constants.ALGORITHM);
    }

    public byte [] encrypt(byte [] packetBytes, byte[] pWord){
        ByteBuffer buffer = ByteBuffer.allocate(128);
        buffer.put(pWord);
        setKey(buffer.array());
        try {
            Cipher cipher = Cipher.getInstance(Constants.EXT_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(packetBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte [] decrypt(byte [] packetBytes, byte [] pWord){
        ByteBuffer buffer = ByteBuffer.allocate(128);
        buffer.put(pWord);
        setKey(buffer.array());

        try{
            Cipher cipher = Cipher.getInstance(Constants.EXT_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(packetBytes);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
