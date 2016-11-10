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
    
    class Test{
      var messageObservable = StandardObservableProperty("Initial Message")
      var message by messageObservable
      
      fun test(){
        println(message) //you can access the value at any time
        
        //The code below is adding a new listener to the observable.  This listener will be called whenever the observable changes.
        messageObservable += { newMessage ->
          println("Message has changed to: $newMessage")
        }
        
        message = "This is a new message." //should print "Message has changed to: This is a new message"
      }
      
      
## ObservableList<T>

This interface and its implementors works along the same idea as `ObservableProperty<T>`, except that this one is a list whose changes can be observed.  This particularly useful for animating items in and out of a list in the UI.

All observable lists are mutable.  This may be changed in the future.

Example:

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
