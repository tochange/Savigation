
#ifndef ARRAY_H
#define ARRAY_H


const int BeginSize = 10;

#include <stdio.h>

template<class Datatype>
class Array
{
public:

// -------------------------------------------------------
// Name:        Array::Array
// Description: This constructs the array.
// Arguments:   - p_size: the size of the array.
// -------------------------------------------------------
	Array( int p_size ):m_array(new Datatype[p_size]),m_size(p_size),m_index(0)
    {
    }

	Array():m_array(new Datatype[BeginSize]),m_size(BeginSize),m_index(0)
	{
	}


// -------------------------------------------------------
// Name:        Array::~Array
// Description: This destructs the array.
// -------------------------------------------------------
    ~Array()
    {
        // if the array is not null, delete it.
        if( m_array != 0 )
            delete[] m_array;

        // clear the pointer, just in case we ever modify this.
        m_array = 0;
    }


// -------------------------------------------------------
// Name:        Array::Resize
// Description: This resizes the array to a new size.
// Arguments:   - p_size: the new size of the array.
// -------------------------------------------------------
    void Resize( int p_size )
    {
        // create a new array with the new size
        Datatype* newarray = new Datatype[p_size];

        // if the new array wasn't allocated, then just return
        // and don't change anything.
        if( newarray == 0 )
            return;

        // determine which size is smaller.
        int min;
        if( p_size < m_size )
            min = p_size;
        else
            min = m_size;

        // loop through and copy everything possible over.
        int index;
        for( index = 0; index < min; index++ )
            newarray[index] = m_array[index];

        // set the size of the new array
        m_size = p_size;

        // delete the old array.
        if( m_array != 0 )
            delete[] m_array;

        // copy the pointer over.
        m_array = newarray;
    }


// -------------------------------------------------------
//  Name:         Array::operator[]
//  Description:  gets a reference to the item at given 
//                index.
//  Arguments:    - p_index: index of the item to get.
//  Return Value: reference to the item at the index.
// -------------------------------------------------------
    Datatype& operator[] ( int p_index )
    {
        return m_array[p_index];
    }

	Datatype& getItem(int p_index)
	{
		return m_array[p_index];
	}


	void push_back(Datatype p_item)
	{
		if(m_index + 1 == m_size){
			Resize(m_size * 2);
		}
		// insert the item.
		m_array[m_index] = p_item;

		++m_index;
	}

// -------------------------------------------------------
//  Name:         Array::Size
//  Description:  gets the size of the array.
//  Arguments:    None.
//  Return Value: the size of the array.
// -------------------------------------------------------
    int size()
    {
        return m_index;
    }

	int CapaCity()
	{
		return m_size;
	}


// -------------------------------------------------------
//  Name:         Array::operator DataType*
//  Description:  conversion operator, converts array
//                into a pointer, for use in C-functions
//                and other normal array functions
//  Arguments:    None.
//  Return Value: a pointer to the array.
// -------------------------------------------------------
    operator Datatype* ()
    {
        return m_array;
    }


// -------------------------------------------------------
// Name:        Array::m_array
// Description: This is a pointer to the array.
// -------------------------------------------------------
    Datatype* m_array;


// -------------------------------------------------------
// Name:        Array::m_size
// Description: the current size of the array.
// -------------------------------------------------------
    int m_size;

	int m_index;
};


#endif