/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.constraints.ternary;

import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ICF;
import solver.constraints.IntConstraintFactory;
import solver.search.loop.monitors.IMonitorSolution;
import solver.search.loop.monitors.SMF;
import solver.search.strategy.ISF;
import solver.variables.IntVar;
import solver.variables.VF;
import util.ESat;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/07/12
 */
public class DivTest extends AbstractTernaryTest {

    @Override
    protected int validTuple(int vx, int vy, int vz) {
        return vy != 0 && vz == vx / vy ? 1 : 0;
    }

    @Override
    protected Constraint make(IntVar[] vars, Solver solver) {
        return IntConstraintFactory.eucl_div(vars[0], vars[1], vars[2]);
    }

    @Test(groups = "1s")
    public void testJL() {
        Solver solver = new Solver();
        IntVar i = VF.enumerated("i", 0, 2, solver);
        solver.post(ICF.eucl_div(i, VF.one(solver), VF.zero(solver)).getOpposite());
        SMF.log(solver, true, false);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testJL2() {
        for (int i = 0; i < 100000; i++) {
            final Solver s = new Solver();
            IntVar a = VF.enumerated("a", new int[]{0, 2, 3, 4}, s);
            IntVar b = VF.enumerated("b", new int[]{-1, 1, 3, 4}, s);
            IntVar c = VF.enumerated("c", new int[]{-3, 1, 4}, s);
            s.post(ICF.eucl_div(a, b, c));
            s.set(ISF.random_value(new IntVar[]{a, b, c}, i));
            //SMF.log(s, true, true);
            s.plugMonitor(new IMonitorSolution() {
                @Override
                public void onSolution() {
                    if (!ESat.TRUE.equals(s.isSatisfied())) {
                        throw new Error(s.toString());
                    }
                }
            });
            s.findAllSolutions();
        }
    }
}
