package hu.belicza.andras.util.secure;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

public class RsaKeyPairGen {
	
	private static final int     KEY_SIZE_BITS = 3072;
	
	private static final boolean DEBUG         = false;
	
	
	
	public static void main( final String[] args ) throws Exception {
		final KeyPairGenerator kpGen = KeyPairGenerator.getInstance( "RSA" );
		kpGen.initialize( KEY_SIZE_BITS );
		final KeyPair kp = kpGen.generateKeyPair();
		
		final PublicKey  pubKey  = kp.getPublic();
		final PrivateKey privKey = kp.getPrivate();
	    
		if ( DEBUG ) {
    		System.out.println( pubKey .getAlgorithm() + " " + pubKey .getFormat() + " " + pubKey .getEncoded().length );
    		System.out.println( privKey.getAlgorithm() + " " + privKey.getFormat() + " " + privKey.getEncoded().length );
		}
		
		final KeyFactory kf = KeyFactory.getInstance( "RSA" );
		final RSAPublicKeySpec  pubKeySpec  = kf.getKeySpec( pubKey , RSAPublicKeySpec .class );
		final RSAPrivateKeySpec privKeySpec = kf.getKeySpec( privKey, RSAPrivateKeySpec.class );
		
		if ( DEBUG ) {
			System.out.println( pubKeySpec .getModulus() + " " + pubKeySpec .getPublicExponent() );
			System.out.println( privKeySpec.getModulus() + " " + privKeySpec.getPrivateExponent() );
		}
		
		saveKey( pubKeySpec .getModulus(), pubKeySpec .getPublicExponent (), "w:/pubkey.rsa"  );
		saveKey( privKeySpec.getModulus(), privKeySpec.getPrivateExponent(), "w:/privkey.rsa" );
	}
	
	private static void saveKey( final BigInteger mod, final BigInteger exp, final String fileName ) throws Exception {
		try ( final DataOutputStream out = new DataOutputStream( new FileOutputStream( fileName ) ) ) {
			byte[] buff = mod.toByteArray();
			out.writeInt( buff.length );
			out.write( buff );
			
			buff = exp.toByteArray();
			out.writeInt( buff.length );
			out.write( buff );
		}
	}
	
}
