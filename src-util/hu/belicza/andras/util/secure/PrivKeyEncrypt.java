package hu.belicza.andras.util.secure;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.RSAPrivateKeySpec;

import javax.crypto.Cipher;

public class PrivKeyEncrypt {
	
	private static final String DOCUMENT =
		"This is the document to be encrypted.";
	
	public static void main( final String[] args ) throws Exception {
		final KeyFactory kf = KeyFactory.getInstance( "RSA" );
		
		final BigInteger[] modExp = loadKey( "w:/privkey.rsa" );
		final PrivateKey privKey = kf.generatePrivate( new RSAPrivateKeySpec( modExp[ 0 ], modExp[ 1 ] ) );
		
		final Cipher cipher = Cipher.getInstance( "RSA" );
		cipher.init( Cipher.ENCRYPT_MODE, privKey );
		final byte[] encrypted = cipher.doFinal( DOCUMENT.getBytes( "UTF-8") );
		
		System.out.println( "Successful encryption." );
		
		try ( final FileOutputStream out = new FileOutputStream( "w:/encrypted.dat" ) ) {
			out.write( encrypted );
		}
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
