/*
 * Copyright © 2002-2018 Neo4j Sweden AB (http://neo4j.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opencypher.v9_0.ast

import org.opencypher.v9_0.expressions._
import org.opencypher.v9_0.util.DummyPosition
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite
import org.opencypher.v9_0.expressions
import org.opencypher.v9_0.expressions._

class FindDuplicateRelationshipsTest extends CypherFunSuite {

  val pos = DummyPosition(0)
  val node = NodePattern(None, Seq.empty, None)(pos)
  val relR = Variable("r")(pos)
  val relS = Variable("s")(pos)

  test("find duplicate relationships across pattern parts") {
    val relPath = EveryPath(RelationshipChain(node, relPattern(relR), node)(pos))
    val pattern = Pattern(Seq(relPath, relPath))(pos)

    Pattern.findDuplicateRelationships(pattern) should equal(Set(Seq(relR, relR)))
  }

  test("find duplicate relationships in a long rel chain") {
    val relPath = expressions.EveryPath(relChain(relR, relS, relR))
    val pattern = Pattern(Seq(relPath))(pos)

    Pattern.findDuplicateRelationships(pattern) should equal(Set(Seq(relR, relR)))
  }

  test("does not find duplicate relationships across pattern parts if there is none") {
    val relPath = EveryPath(expressions.RelationshipChain(node, relPattern(relR), node)(pos))
    val otherRelPath = EveryPath(expressions.RelationshipChain(node, relPattern(relS), node)(pos))
    val pattern = Pattern(Seq(relPath, otherRelPath))(pos)

    Pattern.findDuplicateRelationships(pattern) should equal(Set.empty)
  }

  test("does not find duplicate relationships in a long rel chain if there is none") {
    val relPath = expressions.EveryPath(relChain(relS, relR))
    val pattern = Pattern(Seq(relPath))(pos)

    Pattern.findDuplicateRelationships(pattern) should equal(Set.empty)
  }

  private def relChain(ids: Variable*) =
    ids.foldRight(node.asInstanceOf[PatternElement]) {
      (id, n) => expressions.RelationshipChain(n, relPattern(id), node)(pos)
    }

  private def relPattern(id: Variable) =
    RelationshipPattern(Some(id), Seq(), None, None, SemanticDirection.OUTGOING)(pos)
}
