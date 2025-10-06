# SOLID Principles in Android Development with Kotlin

## Introduction

SOLID is an acronym for five design principles that help create maintainable and scalable codebases.
Applying these ideas in Android projects keeps features isolated, encourages extension without
breaking existing behavior and makes code easier to test.

## Single Responsibility Principle

Each class should have one reason to change. Splitting responsibilities into distinct components
improves clarity and testability.

### Violation

```kotlin
class ItemManager(private val context: Context) {
    private val items: MutableList<Item> = mutableListOf()

    fun retrieveAndDisplayItems() {
        val items = retrieveItemsFromServer()
        val recyclerView = RecyclerView(context)
        val adapter = ItemListAdapter(items)
        recyclerView.adapter = adapter
    }

    fun retrieveItemsFromServer(): List<Item> {
        return emptyList()
    }

    fun storeItemsLocally(items: List<Item>) {
        // Save items to the local database
    }
}
```

### Adherence

```kotlin
class ItemRepository {
    fun fetchItems(): List<Item> {
        // Fetch items from a server or database
        return emptyList()
    }

    fun saveItems(items: List<Item>) {
        // Persist items to a database
    }
}
```

## Open-Closed Principle

Software entities should be open for extension and closed for modification. Favor abstraction so new
behavior can be added without altering existing code.

### Violation

```kotlin
class ItemService {
    fun calculateTotalPrice(cart: List<Item>, discount: Double): Double {
        var totalPrice = cart.sumOf { it.price }
        totalPrice *= (1.0 - discount)
        return totalPrice
    }
}
```

### Adherence

```kotlin
interface PriceCalculator {
    fun calculateTotalPrice(cart: List<Product>): Double
}

class BasicPriceCalculator : PriceCalculator {
    override fun calculateTotalPrice(cart: List<Product>): Double {
        return cart.sumOf { it.price }
    }
}

class DiscountedPriceCalculator(private val discount: Double) : PriceCalculator {
    override fun calculateTotalPrice(cart: List<Product>): Double {
        val basic = BasicPriceCalculator()
        val total = basic.calculateTotalPrice(cart)
        return total * (1.0 - discount)
    }
}
```

## Liskov Substitution Principle

Subclasses must be replaceable for their base types without altering the correctness of the program.

### Violation

```kotlin
open class Bird {
    open fun fly() {
        // Default flying behavior
    }
}

class Dog : Bird() {
    override fun fly() {
        throw UnsupportedOperationException("Dogs can't fly")
    }
}
```

### Adherence

```kotlin
open class Bird {
    open fun move() {
        // Default movement behavior
    }
}

class Ostrich : Bird() {
    override fun move() {
        // Ostriches move by running
    }
}
```

## Interface Segregation Principle

Clients should not be forced to implement methods they do not use. Split broad interfaces into
focused ones.

### Violation

```kotlin
interface Worker {
    fun work()
    fun eat()
}

class SuperWorker : Worker {
    override fun work() {
        // Working behavior
    }

    override fun eat() {
        // Eating behavior
    }
}
```

### Adherence

```kotlin
interface Workable {
    fun work()
}

interface Eatable {
    fun eat()
}

class SuperWorker : Workable, Eatable {
    override fun work() {
        // Working behavior
    }


    override fun eat() {
        // Eating behavior
    }
}
```

## Dependency Inversion Principle

High-level modules should depend on abstractions rather than concrete implementations.

### Violation

```kotlin
class LightBulb {
    fun turnOn() {
        // Turn on the light bulb
    }
}

class Switch {
    private val bulb = LightBulb()

    fun control() {
        bulb.turnOn()
    }
}
```

### Adherence

```kotlin
interface Switchable {
    fun turnOn()
}

class LightBulb : Switchable {
    override fun turnOn() {
        // Turn on the light bulb
    }
}

class Switch(private val device: Switchable) {
    fun control() {
        device.turnOn()
    }
}
```

## Conclusion

Applying the SOLID principles in Android projects encourages separation of concerns, extensibility
and decoupling. These guidelines lead to code that is easier to understand, test and evolve over
time.