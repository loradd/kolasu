package com.strumenta.kolasu.emf

import com.strumenta.kolasu.model.Point
import com.strumenta.kolasu.model.Position
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emfcloud.jackson.resource.JsonResourceFactory
import org.junit.Test
import java.io.File
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ModelTest {

    @Test
    fun generateSimpleModel() {
        val cu = CompilationUnit(
            listOf(
                VarDeclaration(Visibility.PUBLIC, "a", StringLiteral("foo")),
                VarDeclaration(Visibility.PRIVATE, "b", StringLiteral("bar")),
                VarDeclaration(Visibility.PRIVATE, "c", LocalDateTimeLiteral(LocalDateTime.now())),
            ),
            Position(Point(1, 0), Point(1, 1))
        )
        val nsURI = "https://strumenta.com/simplemm"
        val metamodelBuilder = MetamodelBuilder(packageName(CompilationUnit::class), nsURI, "simplemm")
        metamodelBuilder.provideClass(CompilationUnit::class)
        val ePackage = metamodelBuilder.generate()

        val eo = cu.toEObject(ePackage)
        assertEquals(nsURI, eo.eClass().ePackage.nsURI)
        eo.saveXMI(File("simplemodel.xmi"))
        val jsonFile = File("simplem.json")
        eo.saveAsJson(jsonFile)

        val resourceSet = ResourceSetImpl()
        resourceSet.resourceFactoryRegistry.extensionToFactoryMap["json"] = JsonResourceFactory()
        // TODO this is to correctly resolve the metamodel, however what would happen if there were
        // other references to https://... resources?
        resourceSet.resourceFactoryRegistry.protocolToFactoryMap["https"] = JsonResourceFactory()
        val kolasuURI = URI.createURI(KOLASU_METAMODEL.nsURI)
        val kolasuRes = resourceSet.createResource(kolasuURI)
        kolasuRes.contents.add(KOLASU_METAMODEL)
        val metaURI = URI.createURI(nsURI)
        val metaRes = resourceSet.createResource(metaURI)
        metaRes.contents.add(ePackage)
        val uri: URI = URI.createFileURI(jsonFile.absolutePath)
        val resource: Resource = resourceSet.createResource(uri)
        assertFalse(resource.isLoaded)
        resource.load(null)
        assertEquals(1, resource.contents.size)
        assertTrue(resource.contents[0] is EObject)
        val eObject = resource.contents[0] as EObject
        val cuClass = ePackage.eClassifiers.find { c -> c.name.equals("CompilationUnit") } as EClass
        assertEquals(cuClass, eObject.eClass())
        val stmts = eObject.eGet(cuClass.getEStructuralFeature("statements")) as EList<*>
        assertEquals(3, stmts.size)
    }

    @Test
    fun nullCollection() {
        val cu = CompilationUnit(null)
        val nsURI = "https://strumenta.com/simplemm"
        val metamodelBuilder = MetamodelBuilder(packageName(CompilationUnit::class), nsURI, "simplemm")
        metamodelBuilder.provideClass(CompilationUnit::class)
        val ePackage = metamodelBuilder.generate()

        val eo = cu.toEObject(ePackage)
        assertEquals(nsURI, eo.eClass().ePackage.nsURI)
        eo.saveXMI(File("simplemodel_null.xmi"))
        val jsonFile = File("simplem_null.json")
        eo.saveAsJson(jsonFile)

        val resourceSet = ResourceSetImpl()
        resourceSet.resourceFactoryRegistry.extensionToFactoryMap["json"] = JsonResourceFactory()
        // TODO this is to correctly resolve the metamodel, however what would happen if there were
        // other references to https://... resources?
        resourceSet.resourceFactoryRegistry.protocolToFactoryMap["https"] = JsonResourceFactory()
        val metaURI = URI.createURI(nsURI)
        val metaRes = resourceSet.createResource(metaURI)
        metaRes.contents.add(ePackage)
        val uri: URI = URI.createFileURI(jsonFile.absolutePath)
        val resource: Resource = resourceSet.createResource(uri)
        assertFalse(resource.isLoaded)
        resource.load(null)
        assertEquals(1, resource.contents.size)
        assertTrue(resource.contents[0] is EObject)
        val eObject = resource.contents[0] as EObject
        val cuClass = ePackage.eClassifiers.find { c -> c.name.equals("CompilationUnit") } as EClass
        assertEquals(cuClass, eObject.eClass())
        val stmts = eObject.eGet(cuClass.getEStructuralFeature("statements")) as EList<*>
        assertEquals(0, stmts.size)
    }
}
