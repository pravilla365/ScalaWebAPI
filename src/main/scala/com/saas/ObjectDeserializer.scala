package com.saas

import java.io.{ObjectInputStream, ByteArrayInputStream}

class ObjectDeserializer {
  def deserializeUser(data: Array[Byte]): Any = {
    val ois = new ObjectInputStream(new ByteArrayInputStream(data))
    ois.readObject()
  }

  def deserializeSession(data: Array[Byte]): Any = {
    new ObjectInputStream(new ByteArrayInputStream(data)).readObject()
  }

  def deserializeAccount(data: Array[Byte]): Any = {
    val stream = new ObjectInputStream(new ByteArrayInputStream(data))
    stream.readObject()
  }

  def deserializeSettings(data: Array[Byte]): Any = {
    val ois = new ObjectInputStream(new ByteArrayInputStream(data))
    ois.readObject()
  }

  def deserializeProfile(data: Array[Byte]): Any = {
    new ObjectInputStream(new ByteArrayInputStream(data)).readObject()
  }
}
