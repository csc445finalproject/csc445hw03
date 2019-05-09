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

    /*
    SYMMETRIC ALGORITHM ADVANCED ENCRYPTION STANDARD IS USED FOR
    SECURITY BY ENCRYPTING AND DECRYPTING A PASSWORD FOR CONNECTION
    AND TO AVOID UNWANTED ACCESS
     */

    private SecretKeySpec secretKey;

    /**
     *
     * @param pWord generate a secret key that will be used for encryption and decryption
     */

    private void setKey(byte [] pWord){
        secretKey = new SecretKeySpec(pWord, Constants.ALGORITHM);
    }

    /**
     * Uses the java built in AES library to encrypt a password that has been set by the
     * server which is 16 characters or less
     * @param packetBytes is the incoming byte array that is being encrypted and
     *                    does the specified transformation
     * @param pWord is the password byte array that is being encrypted
     * @return
     */


    public byte [] encrypt(byte [] packetBytes, byte[] pWord){
        ByteBuffer buffer = ByteBuffer.allocate(Constants.KEY_SIZE);
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

    /**client
     * Decryption of the encrypted password
     * @param packetBytes the encrypted packet bytes are decrypted which is essentially
     *                    the encryption process done backwards
     * @param pWord the incoming password that has been encrypted is decrypted
     * @return
     */

    public byte [] decrypt(byte [] packetBytes, byte [] pWord){
        ByteBuffer buffer = ByteBuffer.allocate(Constants.KEY_SIZE);
        buffer.put(pWord);
        setKey(buffer.array());

        try{
            Cipher cipher = Cipher.getInstance(Constants.EXT_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(packetBytes);
        } catch (Exception e){
            Client.passwordCorrect = false;
        }
        return null;
    }
}
