package com.jeekrs.unidef
package languages.common

import utils.ExtKey

trait ExtKeyProvider {
  def keysOnField: List[ExtKey] = List()
  def keysOnFuncDecl: List[ExtKey] = List()
  def keysOnClassDecl: List[ExtKey] = List()
}
