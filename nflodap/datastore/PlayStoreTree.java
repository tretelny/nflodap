/* This file is part of NFLODAP, an On-Line Analytics Processing program for
   NFL plays. It creates various graphs of historic play data given the teams
   and the conditons of the wanted plays.

    Copyright (C) 2013   Ezra Erb

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 3 as published
    by the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    I'd appreciate a note if you find this program useful or make
    updates. Please contact me through LinkedIn or github (my profile also has
    a link to the code depository)
*/
package nflodap.datastore;

import java.util.*;

/* This class is part of the internal storage for NFL play data. Since
   category characteristics are all specified as enums, the natural data design
   is a set of nested enummaps. Using them like this, though, is difficult,
   thanks to Java's strict typesafety and lack of typedefs. The same code must
   be implemented over and over, once for every level of nested maps. Such code
   is tedious to write and nearly impossible to update. An alternative is
   using wildcards, but they are difficult to set up correctly with enums.
   Instead, this code uses the Composite pattern to implement the cube as a
   very wide tree. Every entry for a given enum value has pointers to the
   entries for the next layer of enum values. The memory usage is only slightly
   larger than the nested maps.

   Data is read out of the class using an iterator. It produces one list of
   plays for each set of category values with plays defined. The lists are
   copied from the data store to protect its contents. The category values
   are NOT reported; the caller should know the dimensions of the data store
   (which can change thanks to pivot calls) and read them directly from the
   plays. Any operation that modifies the data store will invalidate all
   iterators. */
/* NOTE: To protect the consistency of the data, all public access should be
   done through a single class. This class is deliberately restricted to the
   package */
interface PlayStoreTree extends Cloneable
{
    // Returns true if the data store is empty
    public boolean empty();
    
    // Insert play into the datastore
    public void insertPlay(SinglePlay play);
    
    // Clone the datastore
    public Object clone() throws CloneNotSupportedException;
    
    /* Remove all plays that do not have the passed enumerated value. If the
       value type does not exist in the play (possible thanks to the generic
       type) nothing happens. This implements the ODAP slice operation */
    public <P extends Enum<P>> void slice(P value, Class<P> valueType);
    
    /* Removes all plays whose integer field value is outside the specified
       range. This implements a version of slice for these fields */
    public void slice(SinglePlay.NumericFields field,
                      IntegerRange wantRange);
    
    // Rolls up the entire contents into the passed play list
    public void rollup(ArrayList<SinglePlay> plays);
    
    /* Rolls the contents into the passed enumerated map, splitting
       plays by the category of the enumerated type */
    // NOTE: K is used by DataStoreInterface, end up with L
    public <L extends Enum<L>> void pivot(EnumMap<L, ArrayList<SinglePlay>> plays, Class<L> enumType);
    
    /* Rolls the contents into the passed double layered enumerated map,
       splitting plays by the category of the enumerated types */
    // NOTE: K is used by DataStoreInterface, end up with L and M
    public <L extends Enum<L>, M extends Enum<M>> void pivot(EnumMap<L, EnumMap<M, ArrayList<SinglePlay>>> plays, Class<L> firstEnumType, Class<M> secondEnumType);
    
    /* Iterator on the data store. It is actually implemented by each
       subclass using the factory pattern. Since this interface is private,
       it is not marked as iterable */
    public Iterator<ArrayList<SinglePlay>> iterator();
    
    /* Appends the contents of this portion of the tree to an output
       buffer. 'indexes' contains the index values of the objects above
       this one used to reach this object. They ultimately end up in the
       output */
    public void toOutput(StringBuffer indexes, StringBuffer output);
}

