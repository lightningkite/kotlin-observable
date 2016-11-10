# kotlin-observable

This package is not Android-specific, and can be used across platforms.

This package contains two major things: `ObservableProperty<T>` and `ObservableList<T>`.
Both are used primarily to connect user interfaces to data seamlessly and worry-free, much like you can use Rx.
This library, however, is meant to be as basic and simple as possible while still implementing some useful features.

## Overall Philosophy

*Complete reuse:*  Everything that is commonly used should be included, preferably as an inline 
extension function to reduce the app's method count.

*Intuitiveness:*  Use inline extension functions to make common tasks simple, such as requesting a 
photo from the user.  In standard Android, doing this properly would take at least a large file by 
itself.  We have extension functions that do it in one line.

## ObservableProperty<T>

An observable property is simply a value container that reports whenever the value is changed.  To boil it down even further, an observable property:

- Is, in itself, collection of lambdas
- Contains value which can be read at any time, called `value`
- Implements `getValue` and `setValue` to enable use as a property delegate.
  
Whenever that value changes, the observable property is expected to call all of its lambdas with the new value as the input.

Example:
```kotlin
class Test{
  var messageObservable = StandardObservableProperty("Initial Message")
  var message by messageObservable

  fun test(){
    println(messageObservable.value) //you can access the value at any time
    println(message) //equivalent to the above

    //The code below is adding a new listener to the observable.  This listener will be called whenever the observable changes.
    messageObservable += { newMessage ->
      println("Message has changed to: $newMessage")
    }

    message = "This is a new message." //should print "Message has changed to: This is a new message"
    
    messageObservable.value = "Other message" //should print "Message has been changed to: Other message"
  }
}
```

### Types of ObservableProperty<T>

You will probably only ever create `StandardObservableProperty` directly.  The others are for rare scenarios or are better created using extension functions on other observable properties.

- MutableObservableProperty - An observable property that is mutable.
- StandardObservableProperty - A standard implementation of an observable property.
- LateInitObservableProperty - An observable property that doesn't have to be set in the constructor, but is non-null.
- ObservablePropertyReference - Wraps an already existing variable as an observable property.  The lambdas will only be called if the variable is set through this object.

### Useful transformations

`observable.sub(lifecycle, {item -> item.childObservable })`

Creates an observable that changes whenever the item in the parent observable changes OR the child observable changes.

`observable.subOpt(lifecycle, {item -> item?.childObservable })`

Creates an observable that changes whenever the item in the parent observable changes OR the child observable changes.
For this one, the item can be null and the child observable can be null.

`observable.mapObservable<S,T>({s:S -> s.t}, {t:T -> t.s})`

Maps an observable.

`observable.mapReadOnly{ convert(it) }`

Maps an observable, but the resulting observable is read only.

      
## ObservableList<T>

This interface and its implementors works along the same idea as `ObservableProperty<T>`, except that this one is a list whose changes can be observed.  This particularly useful for animating items in and out of a list in the UI.

All observable lists are mutable.  This may be changed in the future.

Example:

```kotlin
//Creating an observable list
val myList = observableListOf(1, 2, 3)

//Adding a listener for changes in the list
myList.onUpdate += { list ->
  println("The list was changed!")
}

//Adding a listener for additions to the list
myList.onAdd += { item, index ->
  println("The item $item was added at index $index")
}

myList.add(4)
//Outputs:
//The list was changed!
//The item 4 was added at index 3
```

### Useful transformations

#### No disposal needed

`list.mapping{ item -> /*transformation*/ }`

Creates an observable list that wraps another observable list, passing on the events and always reflecting the original list, but with some transformation applied to each item as it comes through.  The result is non-mutable, however, if you pass in a reverse-mapping lambda, it can be.

#### Disposal needed to disconnect listeners

These transformations require that the resulting wrapper be `dispose`d to disconnect listeners to the original list.  If the original list will be disposed of anyways, however, it doesn't matter.  This means that only the first transformation of these needs to be disposed.

The easiest way to mark one of these for disposal is to attach it to a lifecycle.  Most of these transformations have an optional first argument which takes a lifecycle.

`list.sorting{a, b -> a lessThan b}`

Creates a wrapper around the observable list that is always sorted according to the algorithm given.  It is mutable, but there is no guarentee where the items you add will end up.

`list.filtering{ item -> item.shouldPass() }`

Creates a wrapper around the observable list that filters out undesired elements.  The filter is mutable within the created wrapper.

`list.groupingBy{ item -> item.propertyToGroupBy }`

Creates an observable list of observable lists of items grouped by a certain algorithm, which reflects changes in the original list.

`listOfLists.flatMapping{ item -> item.observableList }`

Creates one large observable list from a list of lists of items, which reflects changes in the both the parent list and the child lists.
