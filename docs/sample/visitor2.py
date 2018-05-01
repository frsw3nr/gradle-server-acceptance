# -*- coding: utf-8 -*-
from abc import ABCMeta, abstractmethod

class ICarElementVisitor(metaclass=ABCMeta):
    """
    Interface like in Python
    """
    @abstractmethod
    def visit_Wheel(self, wheel): pass

    @abstractmethod
    def visit_Engine(self, engine): pass

    @abstractmethod
    def visit_Body(self, body): pass

    @abstractmethod
    def visit_Car(self, car): pass

class ICarElement(metaclass=ABCMeta):
    """
    Interface like in Python
    """
    @abstractmethod
    def accept(self, visitor): pass

class Wheel(ICarElement):
    def __init__(self, name):
        self.name = name

    def accept(self, visitor):
        visitor.visit_Wheel(self)

class Engine(ICarElement):
    def accept(self, visitor):
        visitor.visit_Engine(self)

class Body(ICarElement):
    def accept(self, visitor):
        visitor.visit_Body(self)

class Car(ICarElement):

    def __init__(self):
        self.elements = [
            Wheel('front left'), Wheel('front right'),
            Wheel('back left'), Wheel('back right'),
            Body(), Engine(),
        ]

    def accept(self, visitor):
        for elem in self.elements:
            elem.accept(visitor)
        visitor.visit_Car(self)

class PrintVisitor(ICarElementVisitor):
    def visit_Wheel(self, wheel):
        print('Visiting {} wheel'.format(wheel.name))

    def visit_Engine(self, engine):
        print('Visiting engine')

    def visit_Body(self, body):
        print('Visiting body')

    def visit_Car(self, car):
        print('Visiting car')

class DoVisitor(ICarElementVisitor):
    def visit_Wheel(self, wheel):
        print('Kicking my {} wheel'.format(wheel.name))

    def visit_Engine(self, engine):
        print('Starting my engine')

    def visit_Body(self, body):
        print('Moving my body')

    def visit_Car(self, car):
        print('Starting my car')

def main():
    """
    >>> main()
    Visiting front left wheel
    Visiting front right wheel
    Visiting back left wheel
    Visiting back right wheel
    Visiting body
    Visiting engine
    Visiting car
    --------------------------------
    Kicking my front left wheel
    Kicking my front right wheel
    Kicking my back left wheel
    Kicking my back right wheel
    Moving my body
    Starting my engine
    Starting my car
    """
    car = Car()
    car.accept(PrintVisitor())
    print('-' * 32)
    car.accept(DoVisitor())

main()
