/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.tensorflow.core.pythonrunning;

import java.io.Serializable;
import java.util.function.Supplier;
import org.apache.ignite.Ignite;
import org.apache.ignite.tensorflow.core.ProcessManager;
import org.apache.ignite.tensorflow.core.ProcessManagerWrapper;
import org.apache.ignite.tensorflow.core.nativerunning.NativeProcess;
import org.apache.ignite.tensorflow.core.nativerunning.NativeProcessManager;

/**
 * Python process manager that allows to  start, stop and make other actions with python processes.
 */
public class PythonProcessManager extends ProcessManagerWrapper<NativeProcess, PythonProcess> {
    /** */
    private static final long serialVersionUID = -7095409565854538504L;

    /**
     * Constructs a new instance of python process manager.
     *
     * @param igniteSupplier Ignite instance supplier.
     * @param <T> Type of serializable supplier.
     */
    public <T extends Supplier<Ignite> & Serializable> PythonProcessManager(T igniteSupplier) {
        this(new NativeProcessManager(igniteSupplier));
    }

    /**
     * Constructs a new instance of python process manager.
     *
     * @param delegate Delegate.
     */
    public PythonProcessManager(ProcessManager<NativeProcess> delegate) {
        super(delegate);
    }

    /** {@inheritDoc} */
    @Override protected NativeProcess transformSpecification(PythonProcess spec) {
        return new NativeProcess(
            new PythonProcessBuilderSupplier(),
            spec.getStdin(),
            spec.getNodeId()
        );
    }

    /**
     * Python process builder supplier that is used to create Python process builder.
     */
    private static class PythonProcessBuilderSupplier implements Supplier<ProcessBuilder>, Serializable {
        /** */
        private static final long serialVersionUID = 8497087649461965914L;

        /** Python environment variable name. */
        private static final String PYTHON_ENV_NAME = "PYTHON";

        /** {@inheritDoc} */
        @Override public ProcessBuilder get() {
            String python = System.getenv(PYTHON_ENV_NAME);

            if (python == null)
                python = "python3";

            return new ProcessBuilder(python, "-i");
        }
    }
}
