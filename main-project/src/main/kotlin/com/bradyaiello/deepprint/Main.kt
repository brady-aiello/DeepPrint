package com.bradyaiello.deepprint


fun main() {
    val sample = SampleClass(0.5f, 2.6f, "A point")
    println(sample.deepPrint(0))
    val person = SamplePersonClass(name="Dave", sampleClass=sample)
//    //println(person.deepPrint(0))
//
//    val allTypes = AllTypes()
//    //println(allTypes.deepPrint(0))
//    val threeDeep = ThreeClassesDeep(age = 37, person = person)
//    println(threeDeep.deepPrint(0))
//    //println(threeDeep.toString())
//
//    val threeDeep2 = ThreeClassesDeep2(person = person, age = 37)
//    println(threeDeep2.deepPrint(0))
//    //println(sample.toString())
//
    val threeDeep2Wide = ThreeClassesDeep3(
        person = person,
        age = 55,
        sampleClass = sample
    )
    println(threeDeep2Wide.deepPrint())
}