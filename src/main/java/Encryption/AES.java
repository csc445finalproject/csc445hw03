package Encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AES {

    private SecretKeySpec secretKey;

    private void setKey(byte [] pWord, String algorithm){
        secretKey = new SecretKeySpec(pWord, algorithm);
    }

    public byte [] encrypt(byte [] packetBytes, byte[] pWord, String algorithm, String extAlgorithm){

        setKey(pWord, algorithm);
        try {
            Cipher cipher = Cipher.getInstance(extAlgorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(packetBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte [] decrypt(byte [] packetBytes, byte [] pWord, String algorithm, String extAlgorithm){
        setKey(pWord, algorithm);

        try{
            Cipher cipher = Cipher.getInstance(extAlgorithm);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(packetBytes);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
