/*
 * Copyright 2015 ZSDN Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jmf.data;

/**
 * Lifecycle state of a module, dead (shut down), inactive (standby) or active
 * Created on 7/25/15.
 * @author Jonas Grunert
 */
public enum ModuleLifecycleState {
    Dead, // = 0
    Inactive, // = 1
    Active // = 2
}