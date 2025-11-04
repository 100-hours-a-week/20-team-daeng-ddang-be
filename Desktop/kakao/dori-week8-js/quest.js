class myPet {
    constructor(name, type) {
        this.name = name;
        this.type = type;
    }
}

class Person {
    constructor(name, age) {
        this.name = name;
        this.age = age;
    }

    
    greet() {
        return `Hello my name is ${this.name} and I am ${this.age} years old`;
    }
    
}

const pet = new myPet("Momo", "cat");
const person = new Person("Jane Doe", 25)
console.log(pet.name, pet.type);
console.log(person.greet());

const numbers = [1, 2, 3, 4, 5];
console.log(numbers.reduce ((acc, num) => acc + num, 0));
const doubleNum = numbers.map((x) => x * 2);
console.log(doubleNum);

const add = (a, b) => a + b;
console.log(add(2, 3));

console.log("함수형 패러다임 - 미니 퀘스트")
const sum = add(2, 3);
console.log(sum);
const sumArray = (array) => {
    let sum = 0;
    for (let i = 0; i < array.length; i++) {
        sum += array[i];
    }
    return sum;
}
const array = [1, 2, 3, 4, 5];
console.log(sumArray(array));