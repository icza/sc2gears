/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.common.server;

/**
 * Server side utility class.
 * 
 * @author Andras Belicza
 */
public class CommonUtils {
	
	/**
	 * Database package offered to the users.
	 * @author Andras Belicza
	 */
	public static enum DbPackage {
		// Must be in ascending order!
		FREE        ( "Free"        ,  0,      5_000_000L, true , "UNRANKED"    ),
		BRONZE_SMALL( "Bronze-small",  2,    200_000_000L, false, "BRONZE"      ), // Legacy DB package, not sold anymore
		BRONZE      ( "Bronze"      ,  5,    250_000_000L, true , "BRONZE"      ),
		SILVER      ( "Silver"      , 12,  1_000_000_000L, true , "SILVER"      ),
		GOLD        ( "Gold"        , 19,  2_000_000_000L, true , "GOLD"        ),
		PLATINUM    ( "Platinum"    , 35,  4_000_000_000L, true , "PLATINUM"    ),
		DIAMOND     ( "Diamond"     , 49,  6_000_000_000L, true , "DIAMOND"     ),
		MASTER      ( "Master"      , 64,  8_000_000_000L, true , "MASTER"      ),
		GRANDMASTER ( "Grandmaster" , 79, 10_000_000_000L, true , "GRANDMASTER" );
		
		/** Name of the DB package.                          */
		public final String  name;
		/** Price of the DB package in USD.                  */
		public final int     priceUSD;
		/** Storage that comes with the DB package in bytes. */
		public final long    storage;
		/** Tells if the DB package is buyable.              */
		public final boolean buyable;
		/** Icon name of the DB package.                     */
		public final String  iconName;
		
		/**
		 * Creates a new DbPackage.
		 * @param name     name of the DB package
		 * @param priceUSD price of the DB package in USD
		 * @param storage  storage that comes with the DB package in bytes
		 * @param buyable  tells if the DB package is buyable 
		 * @param iconName icon name of the DB package
		 */
		private DbPackage( final String name, final int priceUSD, final long storage, final boolean buyable, final String iconName ) {
			this.name     = name;
			this.priceUSD = priceUSD;
			this.storage  = storage;
			this.buyable  = buyable;
			this.iconName = iconName;
		}
		
		/**
		 * Returns the DB package specified by the price.
		 * @param priceUSD price in USD to return the DB package for
		 * @return the DB package specified by the price; or <code>null</code> if the price does not meet any DB packages
		 */
		public static DbPackage getFromPayment( float priceUSD ) {
			priceUSD += 0.01; // To get rid of rounding errors
			
			final DbPackage[] dbPackages = values();
			for ( int i = dbPackages.length - 1; i >= 0; i-- )
				if ( priceUSD >= dbPackages[ i ].priceUSD )
					return dbPackages[ i ];
			
			return null;
		}
		
		/**
		 * Returns the DB package specified by the storage.
		 * @param storage storage to return the DB package for
		 * @return the DB package specified by the storage; or <code>null</code> if no DB package found for the specified storage
		 */
		public static DbPackage getFromStorage( final long storage ) {
			final DbPackage[] packages = values();
			
			for ( int i = packages.length - 1; i >= 0; i-- )
				if ( storage == packages[ i ].storage )
					return packages[ i ];
			
			return null;
		}
	}
	
}
