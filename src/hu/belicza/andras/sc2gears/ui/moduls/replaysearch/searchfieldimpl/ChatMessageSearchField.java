/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl;

import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.sc2replay.model.MessageEvents.Message;
import hu.belicza.andras.sc2gears.sc2replay.model.MessageEvents.Text;
import hu.belicza.andras.sc2gears.settings.Settings.PredefinedList;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.ReplayFilter;

import java.io.File;
import java.util.EnumSet;
import java.util.Set;

/**
 * Search field that filters by chat message.
 * 
 * @author Andras Belicza
 */
public class ChatMessageSearchField extends TextSearchField {
	
	/** Required set of replay content to apply this filter. */
	private static final Set< ReplayContent > REQUIRED_REPLAY_CONTENT_SET = EnumSet.of( ReplayContent.MESSAGE_EVENTS );
	
	/**
	 * Creates a new ChatMessageSearchField.
	 */
	public ChatMessageSearchField() {
		super( Id.CHAT_MESSAGE, PredefinedList.REP_SEARCH_CHAT_MESSAGE );
	}
	
	@Override
	public ReplayFilter getFilter() {
		return textField.getText().length() == 0 ? null : new TextReplayFilter( this ) {
			@Override
			public Set< ReplayContent > getRequiredReplayContentSet() {
				return REQUIRED_REPLAY_CONTENT_SET;
			}
			@Override
			public boolean customAccept( final File file, final Replay replay ) {
				for ( final Message message : replay.messageEvents.messages ) {
					if ( message instanceof Text )
						if ( exactMatch ? ( (Text) message ).text.equals( text ) :  ( (Text) message ).text.contains( text ) )
							return true;
				}
				return false;
			}
		};
	}
	
}
