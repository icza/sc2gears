/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.common.client;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TabBar;

/**
 * The menu.
 * 
 * <p>Implementation is a {@link TabBar}.</p>
 * 
 * @author Andras Belicza
 */
public class Menu extends TabBar {
	
	/**
	 * Menu item that can be added to the menu.
	 * @author Andras Belicza
	 */
	public static interface MenuItem {
		
		/**
		 * Returns the display label of the menu item.
		 * @return the display label of the menu item
		 */
		String getLabel();
		
		/**
		 * Called when the menu item is activated.
		 * @param menu reference to the menu
		 */
		void onActivate( Menu menu );
		
		/**
		 * Returns the page class associated with the menu item.<br>
		 * This menu item will be selected automatically if a page whose class is equal to this
		 * is set with the {@link Menu#setPage(Page)} method.
		 * @return the page class associated with the menu item; can be <code>null</code>
		 */
		Class< ? extends Page > getPageClass();
		
	}
	
	/** The menu items. */
	private final MenuItem[] menuItems;
	/** The page container to add and remove pages. */
	private final Panel      pageContainer;
	/** Reference to the current page.              */
	private Page             currentPage;
	
	/**
	 * Creates a new Menu.
	 */
	public Menu( final MenuItem[] menuItems, final Panel pageContainer ) {
		this.menuItems     = menuItems;
		this.pageContainer = pageContainer;
		
		for ( final MenuItem mi : menuItems )
			addTab( mi.getLabel() );
		
		addSelectionHandler( new SelectionHandler< Integer >() {
			@Override
			public void onSelection( final SelectionEvent< Integer > event ) {
				menuItems[ event.getSelectedItem() ].onActivate( Menu.this );
			}
		} );
	}
	
	/**
	 * Sets the current page.
	 * @param page page to be set
	 */
	public void setPage( final Page page ) {
		if ( currentPage != null )
			pageContainer.remove( currentPage );
		
		pageContainer.add( currentPage = page );
		
		for ( int i = 0; i < menuItems.length; i++ )
			if ( page.getClass().equals( menuItems[ i ].getPageClass() ) ) {
				selectTab( i, false );
				break;
			}
		
		page.buildGUI();
	}
	
	/**
	 * Refreshes the menu item labels.
	 * @param indices optional indices to refresh; if not provided, all menu items will be refreshed
	 */
	public void refreshLabels( final int... indices ) {
		if ( indices.length > 0 ) {
			for ( final int i : indices )
				setTabText( i, menuItems[ i ].getLabel() );
		}
		else {
    		for ( int i = 0; i < menuItems.length; i++ )
    			setTabText( i, menuItems[ i ].getLabel() );
		}
	}
	
}
