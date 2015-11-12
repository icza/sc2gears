package hu.belicza.andras.util.secure;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

public class PubKeyDecrypt {
	
	public static void main( final String[] args ) throws Exception {
		final KeyFactory kf = KeyFactory.getInstance( "RSA" );
		
		final BigInteger[] modExp = loadKey( "w:/pubkey.rsa" );
		final PublicKey pubKey = kf.generatePublic( new RSAPublicKeySpec( modExp[ 0 ], modExp[ 1 ] ) );
		
		final File encryptedFile = new File( "w:/encrypted.dat" );
		final byte[] encrypted;
		try ( final FileInputStream in = new FileInputStream( encryptedFile ) ) {
			encrypted = new byte[ (int) encryptedFile.length() ];
			in.read( encrypted );
		}
		
		final long start = System.nanoTime();
		final Cipher cipher = Cipher.getInstance( "RSA" );
		cipher.init( Cipher.DECRYPT_MODE, pubKey );
		final byte[] decrypted = cipher.doFinal( encrypted );
		final long end = System.nanoTime();
		final String DOCUMENT = new String( decrypted, "UTF-8" );
		
		System.out.println( "Successful decryption in " + ( ( end - start ) / 1000000L ) + " ms:" );
		System.out.println( DOCUMENT );
	}
	
	private static BigInteger[] loadKey( final String fileName ) throws Exception {
		final BigInteger[] modExp = new BigInteger[ 2 ];
		
		try ( final DataInputStream in = new DataInputStream( new FileInputStream( fileName ) ) ) {
			byte[] buff = new byte[ in.readInt() ];
			in.read( buff );
			modExp[ 0 ] = new BigInteger( buff );
			
			buff = new byte[ in.readInt() ];
			in.read( buff );
			modExp[ 1 ] = new BigInteger( buff );
		}
		
		return modExp;
	}
	
}
